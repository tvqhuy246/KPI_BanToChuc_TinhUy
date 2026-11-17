package vn.btctu.daklak.kpi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.btctu.daklak.kpi.model.User;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    List<User> findByDepartmentId(Long departmentId);
    List<User> findByRole(String role);
    List<User> findByManagerId(Long managerId);
    List<User> findByOrgLevel(Integer orgLevel);

    @Query("SELECT u FROM User u WHERE u.status = 'ACTIVE' ORDER BY u.orgLevel ASC, u.department.id ASC, u.position.id ASC")
    List<User> findAllActiveUsers();

    @Query("SELECT u FROM User u WHERE u.status = 'ACTIVE' ORDER BY u.orgLevel ASC, u.id ASC")
    List<User> findAllForOrgChart();

    // Lấy danh sách cán bộ dưới quyền trực tiếp hoặc gián tiếp
    @Query("SELECT u FROM User u WHERE u.manager.id = :managerId OR u.manager.manager.id = :managerId OR u.manager.manager.manager.id = :managerId")
    List<User> findSubordinatesRecursive(@Param("managerId") Long managerId);
}
