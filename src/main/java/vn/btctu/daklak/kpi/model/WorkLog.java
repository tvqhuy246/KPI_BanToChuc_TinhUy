package vn.btctu.daklak.kpi.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "work_logs")
public class WorkLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "task_id")
    private Task task;

    @Column(name = "log_date", nullable = false)
    private LocalDate logDate;

    @Column(name = "week_number", nullable = false)
    private Integer weekNumber;

    @Column(name = "month_val", nullable = false)
    private String monthVal;

    @Column(name = "work_content", nullable = false, columnDefinition = TEXT)
    private String workContent;

    @Column(name = "hours_spent")
    private Double hoursSpent = 8.0;

    @Column(name = "result_status")
    private String resultStatus = HOAN_THANH; // HOAN_THANH, DANG_XU_LY, VUONG_MAC

    @Column(name = "supervisor_comment", columnDefinition = TEXT)
    private String supervisorComment;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public WorkLog() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Task getTask() { return task; }
    public void setTask(Task task) { this.task = task; }
    public LocalDate getLogDate() { return logDate; }
    public void setLogDate(LocalDate logDate) { this.logDate = logDate; }
    public Integer getWeekNumber() { return weekNumber; }
    public void setWeekNumber(Integer weekNumber) { this.weekNumber = weekNumber; }
    public String getMonthVal() { return monthVal; }
    public void setMonthVal(String monthVal) { this.monthVal = monthVal; }
    public String getWorkContent() { return workContent; }
    public void setWorkContent(String workContent) { this.workContent = workContent; }
    public Double getHoursSpent() { return hoursSpent; }
    public void setHoursSpent(Double hoursSpent) { this.hoursSpent = hoursSpent; }
    public String getResultStatus() { return resultStatus; }
    public void setResultStatus(String resultStatus) { this.resultStatus = resultStatus; }
    public String getSupervisorComment() { return supervisorComment; }
    public void setSupervisorComment(String supervisorComment) { this.supervisorComment = supervisorComment; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
