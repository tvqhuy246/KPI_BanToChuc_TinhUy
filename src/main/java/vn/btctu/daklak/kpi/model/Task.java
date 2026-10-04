package vn.btctu.daklak.kpi.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = TEXT)
    private String description;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assigned_to", nullable = false)
    private User assignedTo;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assigned_by", nullable = false)
    private User assignedBy;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "criteria_id")
    private KpiCriteria criteria;

    // Phân loại: VAN_BAN, HOP, CONG_TAC, CONG_SO
    @Column(name = "task_type")
    private String taskType = VAN_BAN;

    // Độ phức tạp: DE (x1.0), TRUNG_BINH (x1.2), KHO (x1.5), DAC_BIET (x2.0)
    private String complexity = TRUNG_BINH;

    // Nghiệp vụ Họp: Xây dựng bài phát biểu / tham luận được duyệt
    @Column(name = "speech_approved")
    private Boolean speechApproved = false;

    // Nghiệp vụ Đi công tác cơ sở
    @Column(name = "field_trip_location")
    private String fieldTripLocation;

    @Column(name = "field_trip_result", columnDefinition = TEXT)
    private String fieldTripResult;

    // Nghiệp vụ Văn hóa công sở, kỷ cương giờ giấc
    @Column(name = "office_discipline_note", columnDefinition = TEXT)
    private String officeDisciplineNote;

    private String priority = NORMAL; // LOW, NORMAL, HIGH, URGENT

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "completed_date")
    private LocalDate completedDate;

    private Integer progress = 0; // 0 - 100%

    private String status = IN_PROGRESS; // TODO, IN_PROGRESS, COMPLETED, OVERDUE

    @Column(name = "evidence_text", columnDefinition = TEXT)
    private String evidenceText;

    private Double weight = 1.0;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public Task() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public User getAssignedTo() { return assignedTo; }
    public void setAssignedTo(User assignedTo) { this.assignedTo = assignedTo; }
    public User getAssignedBy() { return assignedBy; }
    public void setAssignedBy(User assignedBy) { this.assignedBy = assignedBy; }
    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }
    public KpiCriteria getCriteria() { return criteria; }
    public void setCriteria(KpiCriteria criteria) { this.criteria = criteria; }
    public String getTaskType() { return taskType; }
    public void setTaskType(String taskType) { this.taskType = taskType; }
    public String getComplexity() { return complexity; }
    public void setComplexity(String complexity) { this.complexity = complexity; }
    public Boolean getSpeechApproved() { return speechApproved; }
    public void setSpeechApproved(Boolean speechApproved) { this.speechApproved = speechApproved; }
    public String getFieldTripLocation() { return fieldTripLocation; }
    public void setFieldTripLocation(String fieldTripLocation) { this.fieldTripLocation = fieldTripLocation; }
    public String getFieldTripResult() { return fieldTripResult; }
    public void setFieldTripResult(String fieldTripResult) { this.fieldTripResult = fieldTripResult; }
    public String getOfficeDisciplineNote() { return officeDisciplineNote; }
    public void setOfficeDisciplineNote(String officeDisciplineNote) { this.officeDisciplineNote = officeDisciplineNote; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public LocalDate getCompletedDate() { return completedDate; }
    public void setCompletedDate(LocalDate completedDate) { this.completedDate = completedDate; }
    public Integer getProgress() { return progress; }
    public void setProgress(Integer progress) { this.progress = progress; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getEvidenceText() { return evidenceText; }
    public void setEvidenceText(String evidenceText) { this.evidenceText = evidenceText; }
    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { this.weight = weight; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getTaskTypeBadge() {
        if (VAN_BAN.equals(taskType)) return Văn bản;
        if (HOP.equals(taskType)) return Họp / Phát biểu;
        if (CONG_TAC.equals(taskType)) return Đi công tác;
        return Công sở / Kỷ cương;
    }

    public String getComplexityBadge() {
        if (DE.equals(complexity)) return Dễ (x1.0);
        if (TRUNG_BINH.equals(complexity)) return Trung bình (x1.2);
        if (KHO.equals(complexity)) return Khó (x1.5);
        return Đặc biệt (x2.0);
    }

    public boolean isOverdue() {
        if (COMPLETED.equals(status)) return false;
        return dueDate != null && dueDate.isBefore(LocalDate.now());
    }
}
