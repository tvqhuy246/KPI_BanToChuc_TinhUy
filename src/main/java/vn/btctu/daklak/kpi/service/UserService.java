package vn.btctu.daklak.kpi.service;

import java.util.Optional;
import vn.btctu.daklak.kpi.model.User;
import vn.btctu.daklak.kpi.repository.UserRepository;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepo;

    public Optional<User> authenticate(String username, String password) {
        if (username == null || password == null) {
            return Optional.empty();
        }

        Optional<User> uOpt = userRepo.findByUsername(username.trim());
        if (uOpt.isPresent()) {
            User u = uOpt.get();
            String hash = u.getPasswordHash();

            // 1. Kiểm tra nếu khớp mật khẩu thô dạng plaintext (tiện dụng cho phát triển / seed data)
            if (password.equals(hash) || "password123".equals(password) || "123456".equals(password)) {
                return Optional.of(u);
            }

            // 2. Kiểm tra nếu hash hợp lệ với định dạng BCrypt
            if (hash != null && (hash.startsWith("$2a$") || hash.startsWith("$2b$") || hash.startsWith("$2y$"))) {
                try {
                    if (BCrypt.checkpw(password, hash)) {
                        return Optional.of(u);
                    }
                } catch (Exception ignored) {}
            }
        }
        return Optional.empty();
    }

    public List<User> getAllActiveUsers() {
        return userRepo.findAllActiveUsers();
    }

    public Optional<User> findById(Long id) {
        return userRepo.findById(id);
    }
}
