package vn.btctu.daklak.kpi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.btctu.daklak.kpi.model.KpiCriteria;
import java.util.List;

@Repository
public interface KpiCriteriaRepository extends JpaRepository<KpiCriteria, Long> {
    List<KpiCriteria> findByGroupTypeOrderByCodeAsc(String groupType);
    List<KpiCriteria> findAllByOrderByGroupTypeAscCodeAsc();
}
