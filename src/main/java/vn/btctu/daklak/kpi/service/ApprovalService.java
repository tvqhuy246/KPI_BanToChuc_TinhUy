package vn.btctu.daklak.kpi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.btctu.daklak.kpi.model.Evaluation;
import vn.btctu.daklak.kpi.model.User;
import vn.btctu.daklak.kpi.repository.EvaluationRepository;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ApprovalService {
    @Autowired
    private EvaluationRepository evalRepo;

    // Trưởng phòng duyệt điểm cho chuyên viên
    public Evaluation managerReview(Long evalId, Double managerScore, String managerNotes) {
        Optional<Evaluation> eOpt = evalRepo.findById(evalId);
        if (eOpt.isPresent()) {
            Evaluation e = eOpt.get();
            e.setManagerScore(managerScore);
            e.setFinalScore(managerScore);
            e.setManagerNotes(managerNotes);
            e.setStatus("REVIEWED");
            return evalRepo.save(e);
        }
        return null;
    }

    // Lãnh đạo Ban phê duyệt kết quả cuối cùng
    public Evaluation leaderApprove(Long evalId, User approver, Double finalScore, String ranking, String leaderNotes) {
        Optional<Evaluation> eOpt = evalRepo.findById(evalId);
        if (eOpt.isPresent()) {
            Evaluation e = eOpt.get();
            e.setApprovedBy(approver);
            e.setFinalScore(finalScore);
            e.setRanking(ranking);
            e.setLeaderNotes(leaderNotes);
            e.setStatus("APPROVED");
            e.setApprovedAt(LocalDateTime.now());
            return evalRepo.save(e);
        }
        return null;
    }
}
