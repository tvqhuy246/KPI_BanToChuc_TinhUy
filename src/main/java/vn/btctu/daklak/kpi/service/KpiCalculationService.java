package vn.btctu.daklak.kpi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.btctu.daklak.kpi.model.DepartmentEvaluation;
import vn.btctu.daklak.kpi.model.Evaluation;
import vn.btctu.daklak.kpi.model.Task;
import vn.btctu.daklak.kpi.repository.DepartmentEvaluationRepository;
import vn.btctu.daklak.kpi.repository.EvaluationRepository;
import vn.btctu.daklak.kpi.repository.TaskRepository;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class KpiCalculationService {
    @Autowired
    private EvaluationRepository evalRepo;

    @Autowired
    private DepartmentEvaluationRepository deptEvalRepo;

    @Autowired
    private TaskRepository taskRepo;

    // 1. Tự động tính điểm cá nhân theo Quy định 01-QĐ/BTCTU
    // Tích hợp: Độ khó văn bản, phát biểu họp được duyệt, báo cáo đi công tác, kỷ cương công sở
    public Evaluation calculateAndSave(Evaluation eval, Double scoreA, Double scoreB, Double scoreC, Long userId) {
        List<Task> userTasks = taskRepo.findByAssignedToIdOrderByDueDateAsc(userId);
        
        long overdueCount = userTasks.stream().filter(Task::isOverdue).count();
        double penalty = overdueCount * 3.0; // Mỗi việc quá hạn trừ 3đ

        // Thưởng thêm cho các việc có đóng góp nổi trội
        double bonus = 0.0;
        for (Task t : userTasks) {
            // Họp: có bài phát biểu được duyệt
            if ("HOP".equals(t.getTaskType()) && Boolean.TRUE.equals(t.getSpeechApproved())) {
                bonus += 2.0;
            }
            // Đi công tác: có báo cáo kết quả thực tế
            if ("CONG_TAC".equals(t.getTaskType()) && t.getFieldTripResult() != null && !t.getFieldTripResult().isEmpty()) {
                bonus += 2.0;
            }
            // Văn bản khó hoàn thành đúng hạn
            if ("VAN_BAN".equals(t.getTaskType()) && "KHO".equals(t.getComplexity()) && "COMPLETED".equals(t.getStatus())) {
                bonus += 1.5;
            }
        }
        if (bonus > 5.0) bonus = 5.0; // Giới hạn điểm thưởng tối đa 5đ

        double total = (scoreA != null ? scoreA : 0.0)
                     + (scoreB != null ? scoreB : 0.0)
                     + (scoreC != null ? scoreC : 0.0)
                     + bonus
                     - penalty;

        if (total < 0) total = 0.0;
        if (total > 100) total = 100.0;

        eval.setSelfScore(total);
        eval.setFinalScore(total);

        // Phân loại xếp loại tự động
        if (total >= 90.0 && overdueCount == 0) {
            eval.setRanking("XUAT_SAC");
        } else if (total >= 70.0) {
            eval.setRanking("TOT");
        } else if (total >= 50.0) {
            eval.setRanking("HOAN_THANH");
        } else {
            eval.setRanking("KHONG_HOAN_THANH");
        }

        eval.setEvaluatedAt(LocalDateTime.now());
        return evalRepo.save(eval);
    }

    // 2. Ban Tổ chức đánh giá xếp loại Tập thể các Phòng chuyên môn
    public DepartmentEvaluation evaluateDepartment(DepartmentEvaluation de) {
        double total = (de.getPlanScore() != null ? de.getPlanScore() : 0.0)
                     + (de.getQualityScore() != null ? de.getQualityScore() : 0.0)
                     + (de.getItDigitalScore() != null ? de.getItDigitalScore() : 0.0)
                     + (de.getDisciplineScore() != null ? de.getDisciplineScore() : 0.0)
                     + (de.getAvgStaffScore() != null ? de.getAvgStaffScore() : 0.0);

        if (total > 100.0) total = 100.0;
        de.setTotalScore(total);

        if (total >= 90.0) {
            de.setRanking("XUAT_SAC");
        } else if (total >= 70.0) {
            de.setRanking("TOT");
        } else if (total >= 50.0) {
            de.setRanking("HOAN_THANH");
        } else {
            de.setRanking("KHONG_HOAN_THANH");
        }
        de.setEvaluatedAt(LocalDateTime.now());
        return deptEvalRepo.save(de);
    }
}
