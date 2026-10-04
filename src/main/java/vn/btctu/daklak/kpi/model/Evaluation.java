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
    private String periodType; // MONTH, QUARTER, YEAR

    @Column(name = "period_value", nullable = false)
    private String periodValue; // VD: 'Tháng 10/2026'

    @Column(name = "self_score")
    private Double selfScore = 0.0;

    @Column(name = "manager_score")
    private Double managerScore = 0.0;

    @Column(name = "final_score")
    private Double finalScore = 0.0;

    private String ranking; // XUAT_SAC, TOT, HOAN_THANH, KHONG_HOAN_THANH

    @Column(name = "self_notes", columnDefinition = TEXT)
    private String selfNotes;

    @Column(name = "manager_notes", columnDefinition = TEXT)
    private String managerNotes;

    @Column(name = "leader_notes", columnDefinition = TEXT)
    private String leaderNotes;

    private String status = DRAFT; // DRAFT, SUBMITTED, REVIEWED, APPROVED

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
    public Double getSelfScore() { return selfScore; }
    public void setSelfScore(Double selfScore) { this.selfScore = selfScore; }
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
    public LocalDateTime getEvaluatedAt() { return evaluatedAt; }
    public void setEvaluatedAt(LocalDateTime evaluatedAt) { this.evaluatedAt = evaluatedAt; }
    public User getApprovedBy() { return approvedBy; }
    public void setApprovedBy(User approvedBy) { this.approvedBy = approvedBy; }
    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }

    public String getRankingDisplayName() {
        if (XUAT_SAC.equals(ranking)) return Hoàn thành xuất sắc;
        if (TOT.equals(ranking)) return Hoàn thành tốt;
        if (HOAN_THANH.equals(ranking)) return Hoàn thành nhiệm vụ;
        if (KHONG_HOAN_THANH.equals(ranking)) return Không hoàn thành;
        return Chưa xếp loại;
    }
}
