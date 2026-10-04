package vn.btctu.daklak.kpi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.btctu.daklak.kpi.model.User;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    List<User> findByDepartmentId(Long departmentId);
    List<User> findByRole(String role);

    @Query(SELECT u FROM User u WHERE u.status = 'ACTIVE' ORDER BY u.department.id ASC, u.position.id ASC)
    List<User> findAllActiveUsers();
}
