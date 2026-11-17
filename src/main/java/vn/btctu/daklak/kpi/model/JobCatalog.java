package vn.btctu.daklak.kpi.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "job_catalog")
public class JobCatalog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String code; // A.001 - D.004

    @Column(name = "group_code", nullable = false, length = 10)
    private String groupCode; // A, B, C, D

    @Column(name = "group_name", nullable = false)
    private String groupName;

    @Column(name = "job_title", nullable = false, columnDefinition = "TEXT")
    private String jobTitle;

    @Column(name = "output_product")
    private String outputProduct;

    @Column(name = "complexity_level")
    private Integer complexityLevel = 1; // 1, 2, 3, 4

    @Column(name = "time_limit_str", length = 50)
    private String timeLimitStr;

    @Column(name = "standard_hours")
    private Double standardHours;

    @Column(name = "benchmark_points")
    private Integer benchmarkPoints = 10;

    @Column(name = "is_confidential")
    private Boolean isConfidential = false;

    @Column(name = "security_level", length = 30)
    private String securityLevel = "NOI_BO"; // TUYET_MAT, TOI_MAT, MAT, NOI_BO

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public JobCatalog() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getJobCode() { return code; } // Alias

    public String getGroupCode() { return groupCode; }
    public void setGroupCode(String groupCode) { this.groupCode = groupCode; }
    public String getJobGroup() { return groupCode; } // Alias

    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }
    public String getPositionType() { return groupName; } // Alias

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }
    public String getJobName() { return jobTitle; } // Alias

    public String getOutputProduct() { return outputProduct; }
    public void setOutputProduct(String outputProduct) { this.outputProduct = outputProduct; }
    public String getDeliverableProduct() { return outputProduct; } // Alias

    public Integer getComplexityLevel() { return complexityLevel; }
    public void setComplexityLevel(Integer complexityLevel) { this.complexityLevel = complexityLevel; }

    public String getTimeLimitStr() { return timeLimitStr; }
    public void setTimeLimitStr(String timeLimitStr) { this.timeLimitStr = timeLimitStr; }

    public Double getStandardHours() { return standardHours; }
    public void setStandardHours(Double standardHours) { this.standardHours = standardHours; }

    public Integer getBenchmarkPoints() { return benchmarkPoints; }
    public void setBenchmarkPoints(Integer benchmarkPoints) { this.benchmarkPoints = benchmarkPoints; }
    public Integer getBenchmarkPoint() { return benchmarkPoints; } // Alias

    public Boolean getIsConfidential() { return isConfidential; }
    public void setIsConfidential(Boolean isConfidential) { this.isConfidential = isConfidential; }

    public String getSecurityLevel() { return securityLevel; }
    public void setSecurityLevel(String securityLevel) { this.securityLevel = securityLevel; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getComplexityDisplayName() {
        if (complexityLevel != null) {
            return "Cấp " + complexityLevel;
        }
        return "Cấp 1";
    }

    public String getSecurityBadge() {
        if ("MAT".equals(securityLevel) || Boolean.TRUE.equals(isConfidential)) {
            return "Văn bản Mật";
        }
        return "Nội bộ";
    }
}
