package vn.btctu.daklak.kpi.model;

import jakarta.persistence.*;

@Entity
@Table(name = "evaluation_details")
public class EvaluationDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluation_id", nullable = false)
    private Evaluation evaluation;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "criteria_id", nullable = false)
    private KpiCriteria criteria;

    @Column(name = "self_points")
    private Double selfPoints = 0.0;

    @Column(name = "manager_points")
    private Double managerPoints = 0.0;

    @Column(name = "final_points")
    private Double finalPoints = 0.0;

    @Column(columnDefinition = TEXT)
    private String notes;

    public EvaluationDetail() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Evaluation getEvaluation() { return evaluation; }
    public void setEvaluation(Evaluation evaluation) { this.evaluation = evaluation; }
    public KpiCriteria getCriteria() { return criteria; }
    public void setCriteria(KpiCriteria criteria) { this.criteria = criteria; }
    public Double getSelfPoints() { return selfPoints; }
    public void setSelfPoints(Double selfPoints) { this.selfPoints = selfPoints; }
    public Double getManagerPoints() { return managerPoints; }
    public void setManagerPoints(Double managerPoints) { this.managerPoints = managerPoints; }
    public Double getFinalPoints() { return finalPoints; }
    public void setFinalPoints(Double finalPoints) { this.finalPoints = finalPoints; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
