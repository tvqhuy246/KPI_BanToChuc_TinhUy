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
    List<Evaluation> findByUserIdOrderByEvaluatedAtDesc(Long userId);
    List<Evaluation> findByDepartmentIdAndPeriodValue(Long deptId, String periodValue);
    List<Evaluation> findByPeriodValue(String periodValue);
    Optional<Evaluation> findByUserIdAndPeriodValue(Long userId, String periodValue);

    @Query(SELECT AVG(e.finalScore) FROM Evaluation e WHERE e.periodValue = :period AND e.status = 'APPROVED')
    Double getAverageScoreByPeriod(@Param(period) String period);
}
