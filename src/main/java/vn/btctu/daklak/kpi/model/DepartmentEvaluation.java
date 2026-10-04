package vn.btctu.daklak.kpi.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "department_evaluations")
public class DepartmentEvaluation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(name = "period_type", nullable = false)
    private String periodType; // MONTH, QUARTER, YEAR

    @Column(name = "period_value", nullable = false)
    private String periodValue;

    @Column(name = "plan_score")
    private Double planScore = 0.0; // Tiến độ kế hoạch phòng (30đ)

    @Column(name = "quality_score")
    private Double qualityScore = 0.0; // Chất lượng tham mưu đề án/văn bản (30đ)

    @Column(name = "it_digital_score")
    private Double itDigitalScore = 0.0; // Ứng dụng CNTT, văn phòng số (15đ)

    @Column(name = "discipline_score")
    private Double disciplineScore = 0.0; // Kỷ cương, đoàn kết nội bộ phòng (15đ)

    @Column(name = "avg_staff_score")
    private Double avgStaffScore = 0.0; // Điểm trung bình chuyên viên (10đ)

    @Column(name = "total_score")
    private Double totalScore = 0.0;

    private String ranking; // XUAT_SAC, TOT, HOAN_THANH, KHONG_HOAN_THANH

    @Column(name = "leader_feedback", columnDefinition = TEXT)
    private String leaderFeedback;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "evaluated_by")
    private User evaluatedBy;

    @Column(name = "evaluated_at")
    private LocalDateTime evaluatedAt = LocalDateTime.now();

    public DepartmentEvaluation() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }
    public String getPeriodType() { return periodType; }
    public void setPeriodType(String periodType) { this.periodType = periodType; }
    public String getPeriodValue() { return periodValue; }
    public void setPeriodValue(String periodValue) { this.periodValue = periodValue; }
    public Double getPlanScore() { return planScore; }
    public void setPlanScore(Double planScore) { this.planScore = planScore; }
    public Double getQualityScore() { return qualityScore; }
    public void setQualityScore(Double qualityScore) { this.qualityScore = qualityScore; }
    public Double getItDigitalScore() { return itDigitalScore; }
    public void setItDigitalScore(Double itDigitalScore) { this.itDigitalScore = itDigitalScore; }
    public Double getDisciplineScore() { return disciplineScore; }
    public void setDisciplineScore(Double disciplineScore) { this.disciplineScore = disciplineScore; }
    public Double getAvgStaffScore() { return avgStaffScore; }
    public void setAvgStaffScore(Double avgStaffScore) { this.avgStaffScore = avgStaffScore; }
    public Double getTotalScore() { return totalScore; }
    public void setTotalScore(Double totalScore) { this.totalScore = totalScore; }
    public String getRanking() { return ranking; }
    public void setRanking(String ranking) { this.ranking = ranking; }
    public String getLeaderFeedback() { return leaderFeedback; }
    public void setLeaderFeedback(String leaderFeedback) { this.leaderFeedback = leaderFeedback; }
    public User getEvaluatedBy() { return evaluatedBy; }
    public void setEvaluatedBy(User evaluatedBy) { this.evaluatedBy = evaluatedBy; }
    public LocalDateTime getEvaluatedAt() { return evaluatedAt; }
    public void setEvaluatedAt(LocalDateTime evaluatedAt) { this.evaluatedAt = evaluatedAt; }

    public String getRankingDisplayName() {
        if (XUAT_SAC.equals(ranking)) return Tập thể Xuất sắc;
        if (TOT.equals(ranking)) return Tập thể Tốt;
        if (HOAN_THANH.equals(ranking)) return Tập thể Hoàn thành nhiệm vụ;
        return Tập thể Không hoàn thành;
    }
}
