package vn.btctu.daklak.kpi.model;

import jakarta.persistence.*;

@Entity
@Table(name = "positions")
public class Position {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String title;

    @Column(name = "job_description", columnDefinition = "TEXT")
    private String jobDescription;

    // === CHUẨN TỪ ĐIỂN DỮ LIỆU DÙNG CHUNG PHIÊN BẢN 1.0 (QĐ 308-QĐ/VPTW) ===
    @Column(name = "job_position_code", length = 50)
    private String jobPositionCode; // Mã định danh vị trí việc làm chuẩn hóa Đảng

    public Position() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getJobDescription() { return jobDescription; }
    public void setJobDescription(String jobDescription) { this.jobDescription = jobDescription; }
    public String getJobPositionCode() { return jobPositionCode; }
    public void setJobPositionCode(String jobPositionCode) { this.jobPositionCode = jobPositionCode; }
}
