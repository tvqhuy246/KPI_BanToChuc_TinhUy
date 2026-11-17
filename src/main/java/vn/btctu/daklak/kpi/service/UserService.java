package vn.btctu.daklak.kpi.service;

import java.util.Optional;
import vn.btctu.daklak.kpi.model.Department;
import vn.btctu.daklak.kpi.model.Position;
import vn.btctu.daklak.kpi.model.User;
import vn.btctu.daklak.kpi.repository.DepartmentRepository;
import vn.btctu.daklak.kpi.repository.PositionRepository;
import vn.btctu.daklak.kpi.repository.UserRepository;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepo;

    @Autowired
    private DepartmentRepository deptRepo;

    @Autowired
    private PositionRepository posRepo;

    public Optional<User> authenticate(String username, String password) {
        if (username == null || password == null) {
            return Optional.empty();
        }

        String cleanUser = username.trim();
        if ("qhuy".equalsIgnoreCase(cleanUser)) {
            cleanUser = "tranvoquanghuy";
        }
        Optional<User> uOpt = userRepo.findByUsername(cleanUser);
        if (uOpt.isEmpty() && !"qhuy".equalsIgnoreCase(username.trim())) {
            uOpt = userRepo.findByUsername(username.trim());
        }
        if (uOpt.isPresent()) {
            User u = uOpt.get();
            String hash = u.getPasswordHash();

            if (hash != null && (hash.startsWith("$2a$") || hash.startsWith("$2b$") || hash.startsWith("$2y$"))) {
                try {
                    if (BCrypt.checkpw(password, hash)) {
                        return Optional.of(u);
                    }
                } catch (Exception ignored) {}
            } else if (password != null && password.equals(hash)) {
                return Optional.of(u);
            }
        }
        return Optional.empty();
    }

    public User registerUser(String username, String rawPassword, String fullName, Long departmentId, Long positionId, String email, String phone, String partyMemberCardId, String citizenId) {
        User user = new User();
        user.setUsername(username.trim());
        user.setPasswordHash(BCrypt.hashpw(rawPassword, BCrypt.gensalt(10)));
        user.setFullName(fullName.trim());
        user.setEmail(email != null ? email.trim() : null);
        user.setPhone(phone != null ? phone.trim() : null);
        user.setPartyMemberCardId(partyMemberCardId != null ? partyMemberCardId.trim() : null);
        user.setCitizenId(citizenId != null ? citizenId.trim() : null);

        user.setOrgLevel(5);
        user.setRole("CHUYEN_VIEN");
        user.setStatus("ACTIVE");

        if (departmentId != null) {
            deptRepo.findById(departmentId).ifPresent(dept -> {
                if ("LANH_DAO_BAN".equals(dept.getCode())) {
                    deptRepo.findByCode("VAN_PHONG").ifPresent(user::setDepartment);
                } else {
                    user.setDepartment(dept);
                }

                Department actualDept = user.getDepartment();
                if (actualDept != null) {
                    List<User> deptUsers = userRepo.findByDepartmentId(actualDept.getId());
                    for (User u : deptUsers) {
                        if ("PHO_TRUONG_PHONG".equals(u.getRole())) {
                            user.setManager(u);
                            break;
                        }
                    }
                    if (user.getManager() == null) {
                        for (User u : deptUsers) {
                            if ("TRUONG_PHONG".equals(u.getRole())) {
                                user.setManager(u);
                                break;
                            }
                        }
                    }
                }
            });
        }

        if (positionId != null) {
            posRepo.findById(positionId).ifPresent(pos -> {
                String code = pos.getCode();
                if ("CHUYEN_VIEN".equals(code) || "VAN_THU".equals(code)) {
                    user.setPosition(pos);
                    if ("VAN_THU".equals(code)) {
                        user.setRole("VAN_THU");
                    }
                } else {
                    posRepo.findByCode("CHUYEN_VIEN").ifPresent(user::setPosition);
                }
            });
        }

        // Đảm bảo luôn có vị trí công tác và phòng ban mặc định nếu không truyền
        if (user.getPosition() == null) {
            posRepo.findByCode("CHUYEN_VIEN").ifPresent(user::setPosition);
        }
        if (user.getDepartment() == null) {
            deptRepo.findByCode("VAN_PHONG").ifPresent(user::setDepartment);
        }
        if (user.getManager() == null && user.getDepartment() != null) {
            List<User> deptUsers = userRepo.findByDepartmentId(user.getDepartment().getId());
            for (User u : deptUsers) {
                if ("PHO_TRUONG_PHONG".equals(u.getRole()) || "TRUONG_PHONG".equals(u.getRole())) {
                    user.setManager(u);
                    break;
                }
            }
        }

        return userRepo.save(user);
    }

    public boolean updateUserRoleAndLevel(Long targetUserId, String newRole, Integer newOrgLevel, Long newManagerId, User operatorUser) {
        if (operatorUser == null) return false;

        boolean isAuthorized = "ADMIN".equals(operatorUser.getRole()) || 
                "LANH_DAO_BAN".equals(operatorUser.getRole()) || 
                (operatorUser.getOrgLevel() != null && operatorUser.getOrgLevel() == 1);

        if (!isAuthorized) {
            return false;
        }

        Optional<User> targetOpt = userRepo.findById(targetUserId);
        if (targetOpt.isPresent()) {
            User target = targetOpt.get();
            if (newRole != null && !newRole.trim().isEmpty()) {
                target.setRole(newRole.trim());
            }
            if (newOrgLevel != null && newOrgLevel >= 1 && newOrgLevel <= 5) {
                target.setOrgLevel(newOrgLevel);
            }
            if (newManagerId != null) {
                userRepo.findById(newManagerId).ifPresent(target::setManager);
            }
            userRepo.save(target);
            return true;
        }
        return false;
    }

    /**
     * Cập nhật thông tin lý lịch cán bộ cá nhân
     */
    public boolean updateProfile(Long userId, String fullName, String email, String phone, String partyMemberCardId, String citizenId) {
        Optional<User> opt = userRepo.findById(userId);
        if (opt.isPresent()) {
            User u = opt.get();
            if (fullName != null && !fullName.trim().isEmpty()) {
                u.setFullName(fullName.trim());
            }
            u.setEmail(email != null ? email.trim() : null);
            u.setPhone(phone != null ? phone.trim() : null);
            u.setPartyMemberCardId(partyMemberCardId != null ? partyMemberCardId.trim() : null);
            u.setCitizenId(citizenId != null ? citizenId.trim() : null);
            userRepo.save(u);
            return true;
        }
        return false;
    }

    /**
     * Đổi mật khẩu tài khoản
     */
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        Optional<User> opt = userRepo.findById(userId);
        if (opt.isPresent()) {
            User u = opt.get();
            String hash = u.getPasswordHash();
            boolean oldValid = false;

            if (hash != null && (hash.startsWith("$2a$") || hash.startsWith("$2b$") || hash.startsWith("$2y$"))) {
                try {
                    oldValid = BCrypt.checkpw(oldPassword, hash);
                } catch (Exception ignored) {}
            } else if (oldPassword != null && oldPassword.equals(hash)) {
                oldValid = true;
            }

            if (oldValid) {
                u.setPasswordHash(BCrypt.hashpw(newPassword, BCrypt.gensalt(10)));
                userRepo.save(u);
                return true;
            }
        }
        return false;
    }

    public List<User> getAllActiveUsers() {
        return userRepo.findAllActiveUsers();
    }

    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    public Optional<User> findById(Long id) {
        return userRepo.findById(id);
    }

    public Optional<User> findByUsername(String username) {
        return userRepo.findByUsername(username);
    }
}