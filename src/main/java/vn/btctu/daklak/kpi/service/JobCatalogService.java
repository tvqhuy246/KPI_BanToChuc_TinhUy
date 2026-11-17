package vn.btctu.daklak.kpi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.btctu.daklak.kpi.model.JobCatalog;
import vn.btctu.daklak.kpi.repository.JobCatalogRepository;

import java.util.List;
import java.util.Optional;

@Service
public class JobCatalogService {
    @Autowired
    private JobCatalogRepository jobCatalogRepo;

    public List<JobCatalog> getAllJobs() {
        return jobCatalogRepo.findAllByOrderByCodeAsc();
    }

    public List<JobCatalog> getAllActive() {
        return jobCatalogRepo.findAllByOrderByCodeAsc();
    }

    public List<JobCatalog> getJobsByGroup(String groupCode) {
        return jobCatalogRepo.findByGroupCodeOrderByCodeAsc(groupCode);
    }

    public List<JobCatalog> getByGroup(String groupCode) {
        return jobCatalogRepo.findByGroupCodeOrderByCodeAsc(groupCode);
    }

    public Optional<JobCatalog> getByCode(String code) {
        return jobCatalogRepo.findByCode(code);
    }

    public Optional<JobCatalog> getById(Long id) {
        return jobCatalogRepo.findById(id);
    }

    public List<JobCatalog> searchJobs(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllJobs();
        }
        return jobCatalogRepo.searchByKeyword(keyword.trim());
    }

    public List<JobCatalog> search(String keyword) {
        return searchJobs(keyword);
    }

    /**
     * Logic kiểm tra xem thời gian thực tế có vượt quá định mức tối đa của BTCTU không
     */
    public boolean isOverStandardHours(JobCatalog job, Double actualHours) {
        if (job == null || job.getStandardHours() == null || actualHours == null) {
            return false;
        }
        return actualHours > job.getStandardHours();
    }

    /**
     * Gợi ý điểm chấm chuẩn cho người thẩm định/phê duyệt
     */
    public Integer suggestBenchmarkPoints(JobCatalog job) {
        if (job == null || job.getBenchmarkPoints() == null) {
            return 10;
        }
        return job.getBenchmarkPoints();
    }
}
