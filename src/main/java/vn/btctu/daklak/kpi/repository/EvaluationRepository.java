package vn.btctu.daklak.kpi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.btctu.daklak.kpi.model.Evaluation;
import java.util.List;
import java.util.Optional;

@Repository
public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {
    Optional<Evaluation> findByUserIdAndPeriodValue(Long userId, String periodValue);
    List<Evaluation> findByDepartmentIdAndPeriodValue(Long departmentId, String periodValue);
    List<Evaluation> findByPeriodValue(String periodValue);
    List<Evaluation> findByUserId(Long userId);

    @Query("SELECT AVG(e.finalScore) FROM Evaluation e WHERE e.periodValue = :period AND e.status = 'APPROVED'")
    Double getAverageScoreByPeriod(@Param("period") String period);

    // Smart Routing queries:
    List<Evaluation> findByCurrentApproverIdOrderByEvaluatedAtDesc(Long approverId);
    List<Evaluation> findByCurrentApproverIdAndStatusNot(Long approverId, String status);
    List<Evaluation> findByStatusNotOrderByEvaluatedAtDesc(String status);
    List<Evaluation> findByDepartmentIdAndStatusNotOrderByEvaluatedAtDesc(Long departmentId, String status);
    List<Evaluation> findByStatusInOrderByEvaluatedAtDesc(List<String> statuses);
    List<Evaluation> findByDepartmentIdAndStatusInOrderByEvaluatedAtDesc(Long departmentId, List<String> statuses);
}
