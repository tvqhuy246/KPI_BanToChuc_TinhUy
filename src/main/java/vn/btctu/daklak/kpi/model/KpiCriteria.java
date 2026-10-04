package vn.btctu.daklak.kpi.model;

import jakarta.persistence.*;

@Entity
@Table(name = "kpi_criteria")
public class KpiCriteria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(name = "group_type", nullable = false)
    private String groupType; // A, B, C

    @Column(name = "max_score", nullable = false)
    private Double maxScore;

    @Column(name = "scoring_guide", columnDefinition = TEXT)
    private String scoringGuide;

    @Column(name = "target_role")
    private String targetRole = ALL;

    public KpiCriteria() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getGroupType() { return groupType; }
    public void setGroupType(String groupType) { this.groupType = groupType; }
    public Double getMaxScore() { return maxScore; }
    public void setMaxScore(Double maxScore) { this.maxScore = maxScore; }
    public String getScoringGuide() { return scoringGuide; }
    public void setScoringGuide(String scoringGuide) { this.scoringGuide = scoringGuide; }
    public String getTargetRole() { return targetRole; }
    public void setTargetRole(String targetRole) { this.targetRole = targetRole; }

    public String getGroupDisplayName() {
        if (A.equals(groupType)) return Nhóm A: Tư tưởng, Đạo đức, Kỷ luật (20đ);
        if (B.equals(groupType)) return Nhóm B: Chuyên môn vị trí (70đ);
        return Nhóm C: Sáng kiến & Chuyển đổi số (10đ);
    }
}
