package vn.btctu.daklak.kpi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.btctu.daklak.kpi.model.DepartmentEvaluation;
import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentEvaluationRepository extends JpaRepository<DepartmentEvaluation, Long> {
    List<DepartmentEvaluation> findByPeriodValue(String periodValue);
    Optional<DepartmentEvaluation> findByDepartmentIdAndPeriodValue(Long deptId, String periodValue);
}
