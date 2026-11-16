package vn.btctu.daklak.kpi.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    private String email;
    private String phone;

    // === CHUẨN TỪ ĐIỂN DỮ LIỆU DÙNG CHUNG PHIÊN BẢN 1.0 (QĐ 308-QĐ/VPTW & LUẬT DỮ LIỆU NĐ 165/2025) ===
    @Column(name = "party_member_card_id", length = 50)
    private String partyMemberCardId; // Số thẻ Đảng viên

    @Column(name = "citizen_id", length = 20)
    private String citizenId; // Số CCCD / Định danh công dân

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "position_id")
    private Position position;

    @Column(nullable = false)
    private String role; // ADMIN, LANH_DAO_BAN, PHO_TRUONG_BAN, TRUONG_PHONG, PHO_TRUONG_PHONG, CHUYEN_VIEN, VAN_THU

    private String status = "ACTIVE";

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "manager_id")
    private User manager;

    @Column(name = "org_level")
    private Integer orgLevel = 5;

    @Column(name = "assigned_scope", columnDefinition = "TEXT")
    private String assignedScope;

    public User() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPartyMemberCardId() { return partyMemberCardId; }
    public void setPartyMemberCardId(String partyMemberCardId) { this.partyMemberCardId = partyMemberCardId; }
    public String getCitizenId() { return citizenId; }
    public void setCitizenId(String citizenId) { this.citizenId = citizenId; }

    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }
    public Position getPosition() { return position; }
    public void setPosition(Position position) { this.position = position; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public User getManager() { return manager; }
    public void setManager(User manager) { this.manager = manager; }
    public Integer getOrgLevel() { return orgLevel; }
    public void setOrgLevel(Integer orgLevel) { this.orgLevel = orgLevel; }
    public String getAssignedScope() { return assignedScope; }
    public void setAssignedScope(String assignedScope) { this.assignedScope = assignedScope; }

    public String getRoleDisplayName() {
        if ("LANH_DAO_BAN".equals(role)) return "Trưởng ban";
        if ("PHO_TRUONG_BAN".equals(role)) return "Phó Trưởng ban";
        if ("TRUONG_PHONG".equals(role)) return "Trưởng phòng";
        if ("PHO_TRUONG_PHONG".equals(role)) return "Phó Trưởng phòng";
        if ("VAN_THU".equals(role)) return "Văn thư";
        if ("ADMIN".equals(role)) return "Quản trị viên";
        return "Chuyên viên";
    }

    public String getRoleBadge() {
        return getRoleDisplayName();
    }
}
