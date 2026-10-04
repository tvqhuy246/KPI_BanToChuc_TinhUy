package vn.btctu.daklak.kpi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.btctu.daklak.kpi.model.WorkLog;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface WorkLogRepository extends JpaRepository<WorkLog, Long> {
    List<WorkLog> findByUserIdOrderByLogDateDesc(Long userId);
    List<WorkLog> findByLogDateOrderByCreatedAtDesc(LocalDate date);
    List<WorkLog> findByWeekNumberAndMonthValOrderByLogDateDesc(Integer week, String month);
    List<WorkLog> findByMonthValOrderByLogDateDesc(String month);
}
