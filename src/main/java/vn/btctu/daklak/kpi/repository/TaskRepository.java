package vn.btctu.daklak.kpi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.btctu.daklak.kpi.model.Task;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByAssignedToIdOrderByDueDateAsc(Long userId);
    List<Task> findByDepartmentIdOrderByDueDateAsc(Long deptId);

    @Query(SELECT COUNT(t) FROM Task t WHERE t.assignedTo.id = :userId)
    long countByUserId(@Param(userId) Long userId);

    @Query(SELECT COUNT(t) FROM Task t WHERE t.assignedTo.id = :userId AND t.status = 'COMPLETED')
    long countCompletedByUserId(@Param(userId) Long userId);

    @Query(SELECT COUNT(t) FROM Task t WHERE t.status = 'COMPLETED')
    long countAllCompleted();
}
