package vn.btctu.daklak.kpi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.btctu.daklak.kpi.model.AuditLog;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findTop50ByOrderByCreatedAtDesc();
    List<AuditLog> findTop100ByOrderByCreatedAtDesc();
    List<AuditLog> findByActionOrderByCreatedAtDesc(String action);

    @Query("SELECT a FROM AuditLog a WHERE " +
           "(:action IS NULL OR :action = 'ALL' OR a.action = :action) AND " +
           "(:username IS NULL OR LOWER(a.username) LIKE LOWER(CONCAT('%', :username, '%'))) " +
           "ORDER BY a.createdAt DESC")
    List<AuditLog> searchLogs(@Param("action") String action, @Param("username") String username);
}