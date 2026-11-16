package vn.btctu.daklak.kpi.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "evaluations")
public class Evaluation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(name = "period_type", nullable = false)
    private String periodType = "MONTH"; // MONTH, QUARTER, YEAR

    @Column(name = "period_value", nullable = false)
    private String periodValue; // VD: 'Tháng 10/2026', 'Quý IV/2026', 'Năm 2026'

    @Column(name = "target_type")
    private String targetType = "CHUYEN_VIEN"; // LANH_DAO_QUAN_LY hoặc CHUYEN_VIEN

    @Column(name = "self_score")
    private Double selfScore = 0.0;

    // === CÁC TIÊU CHÍ DÀNH RIÊNG CHO LÃNH ĐẠO, QUẢN LÝ (HD 05-HD/TU & QĐ 39-QĐ/TU) ===
    @Column(name = "leadership_score")
    private Double leadershipScore = 0.0; // Năng lực lãnh đạo, chỉ đạo, điều hành

    @Column(name = "team_progress_score")
    private Double teamProgressScore = 0.0; // Tỷ lệ hoàn thành nhiệm vụ đúng tiến độ của tập thể phòng

    @Column(name = "exemplary_score")
    private Double exemplaryScore = 0.0; // Trách nhiệm nêu gương và giữ gìn đoàn kết nội bộ

    // === HIERARCHICAL APPROVAL ENGINE (4 TẦNG ĐÁNH GIÁ) ===
    @Column(name = "deputy_head_score")
    private Double deputyHeadScore;

    @Column(name = "deputy_head_notes", columnDefinition = "TEXT")
    private String deputyHeadNotes;

    @Column(name = "dept_head_score")
    private Double deptHeadScore;

    @Column(name = "dept_head_notes", columnDefinition = "TEXT")
    private String deptHeadNotes;

    @Column(name = "deputy_leader_score")
    private Double deputyLeaderScore;

    @Column(name = "deputy_leader_notes", columnDefinition = "TEXT")
    private String deputyLeaderNotes;

    @Column(name = "manager_score")
    private Double managerScore = 0.0;

    @Column(name = "final_score")
    private Double finalScore = 0.0;

    private String ranking; // XUAT_SAC, TOT, HOAN_THANH, KHONG_HOAN_THANH

    @Column(name = "self_notes", columnDefinition = "TEXT")
    private String selfNotes;

    @Column(name = "manager_notes", columnDefinition = "TEXT")
    private String managerNotes;

    @Column(name = "leader_notes", columnDefinition = "TEXT")
    private String leaderNotes;

    private String status = "DRAFT"; // DRAFT, SUBMITTED, PENDING_DEPUTY_HEAD, PENDING_DEPT_HEAD, PENDING_DEPUTY_LEADER, PENDING_LEADER, REVIEWED, APPROVED

    @Column(name = "current_step")
    private Integer currentStep = 1;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "current_approver_id")
    private User currentApprover;

    @Column(name = "evaluated_at")
    private LocalDateTime evaluatedAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    public Evaluation() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }
    public String getPeriodType() { return periodType; }
    public void setPeriodType(String periodType) { this.periodType = periodType; }
    public String getPeriodValue() { return periodValue; }
    public void setPeriodValue(String periodValue) { this.periodValue = periodValue; }

    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }

    public Double getSelfScore() { return selfScore; }
    public void setSelfScore(Double selfScore) { this.selfScore = selfScore; }

    public Double getLeadershipScore() { return leadershipScore; }
    public void setLeadershipScore(Double leadershipScore) { this.leadershipScore = leadershipScore; }
    public Double getTeamProgressScore() { return teamProgressScore; }
    public void setTeamProgressScore(Double teamProgressScore) { this.teamProgressScore = teamProgressScore; }
    public Double getExemplaryScore() { return exemplaryScore; }
    public void setExemplaryScore(Double exemplaryScore) { this.exemplaryScore = exemplaryScore; }

    public Double getDeputyHeadScore() { return deputyHeadScore; }
    public void setDeputyHeadScore(Double deputyHeadScore) { this.deputyHeadScore = deputyHeadScore; }
    public String getDeputyHeadNotes() { return deputyHeadNotes; }
    public void setDeputyHeadNotes(String deputyHeadNotes) { this.deputyHeadNotes = deputyHeadNotes; }

    public Double getDeptHeadScore() { return deptHeadScore; }
    public void setDeptHeadScore(Double deptHeadScore) { this.deptHeadScore = deptHeadScore; }
    public String getDeptHeadNotes() { return deptHeadNotes; }
    public void setDeptHeadNotes(String deptHeadNotes) { this.deptHeadNotes = deptHeadNotes; }

    public Double getDeputyLeaderScore() { return deputyLeaderScore; }
    public void setDeputyLeaderScore(Double deputyLeaderScore) { this.deputyLeaderScore = deputyLeaderScore; }
    public String getDeputyLeaderNotes() { return deputyLeaderNotes; }
    public void setDeputyLeaderNotes(String deputyLeaderNotes) { this.deputyLeaderNotes = deputyLeaderNotes; }

    public Double getManagerScore() { return managerScore; }
    public void setManagerScore(Double managerScore) { this.managerScore = managerScore; }
    public Double getFinalScore() { return finalScore; }
    public void setFinalScore(Double finalScore) { this.finalScore = finalScore; }
    public String getRanking() { return ranking; }
    public void setRanking(String ranking) { this.ranking = ranking; }
    public String getSelfNotes() { return selfNotes; }
    public void setSelfNotes(String selfNotes) { this.selfNotes = selfNotes; }
    public String getManagerNotes() { return managerNotes; }
    public void setManagerNotes(String managerNotes) { this.managerNotes = managerNotes; }
    public String getLeaderNotes() { return leaderNotes; }
    public void setLeaderNotes(String leaderNotes) { this.leaderNotes = leaderNotes; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getCurrentStep() { return currentStep; }
    public void setCurrentStep(Integer currentStep) { this.currentStep = currentStep; }
    public User getCurrentApprover() { return currentApprover; }
    public void setCurrentApprover(User currentApprover) { this.currentApprover = currentApprover; }
    public LocalDateTime getEvaluatedAt() { return evaluatedAt; }
    public void setEvaluatedAt(LocalDateTime evaluatedAt) { this.evaluatedAt = evaluatedAt; }
    public User getApprovedBy() { return approvedBy; }
    public void setApprovedBy(User approvedBy) { this.approvedBy = approvedBy; }
    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }

    public String getStatusDisplayName() {
        if ("APPROVED".equals(status)) return "Đã duyệt";
        if ("PENDING_LEADER".equals(status)) return "Chờ Trưởng ban duyệt";
        if ("PENDING_DEPUTY_LEADER".equals(status)) return "Chờ Phó Ban duyệt";
        if ("PENDING_DEPT_HEAD".equals(status)) return "Chờ Trưởng phòng duyệt";
        if ("PENDING_DEPUTY_HEAD".equals(status)) return "Chờ Phó phòng duyệt";
        if ("SUBMITTED".equals(status)) return "Đã nộp";
        return "Bản nháp";
    }

    public String getRankingDisplayName() {
        if ("XUAT_SAC".equals(ranking)) return "Hoàn thành Xuất sắc";
        if ("TOT".equals(ranking)) return "Hoàn thành Tốt";
        if ("HOAN_THANH".equals(ranking)) return "Hoàn thành";
        if ("KHONG_HOAN_THANH".equals(ranking)) return "Không hoàn thành";
        return "Chưa xếp loại";
    }

    public boolean isLeadershipTarget() {
        return "LANH_DAO_QUAN_LY".equals(targetType);
    }
}
