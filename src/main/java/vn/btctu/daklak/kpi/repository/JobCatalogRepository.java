package vn.btctu.daklak.kpi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.btctu.daklak.kpi.model.JobCatalog;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobCatalogRepository extends JpaRepository<JobCatalog, Long> {
    Optional<JobCatalog> findByCode(String code);
    List<JobCatalog> findByGroupCodeOrderByCodeAsc(String groupCode);
    List<JobCatalog> findAllByOrderByCodeAsc();

    @Query("SELECT j FROM JobCatalog j WHERE LOWER(j.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(j.jobTitle) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(j.outputProduct) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY j.code ASC")
    List<JobCatalog> searchByKeyword(@Param("keyword") String keyword);

    List<JobCatalog> findByIsConfidentialTrue();
}
