package vn.btctu.daklak.kpi.service;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.btctu.daklak.kpi.model.User;
import vn.btctu.daklak.kpi.repository.UserRepository;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepo;

    public Optional<User> authenticate(String username, String password) {
        Optional<User> uOpt = userRepo.findByUsername(username);
        if (uOpt.isPresent()) {
            User u = uOpt.get();
            if (BCrypt.checkpw(password, u.getPasswordHash())) {
                return Optional.of(u);
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
