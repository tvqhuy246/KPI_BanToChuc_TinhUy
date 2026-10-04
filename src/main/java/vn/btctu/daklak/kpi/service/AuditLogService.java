package vn.btctu.daklak.kpi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.btctu.daklak.kpi.model.AuditLog;
import vn.btctu.daklak.kpi.repository.AuditLogRepository;
import java.util.List;

@Service
public class AuditLogService {
    @Autowired
    private AuditLogRepository auditLogRepo;

    public void log(Long userId, String username, String action, String entityName, Long entityId, String details, String ip) {
        AuditLog al = new AuditLog(userId, username, action, entityName, entityId, details, ip != null ? ip : 127.0.0.1);
        auditLogRepo.save(al);
    }

    public List<AuditLog> getRecentLogs() {
        return auditLogRepo.findTop50ByOrderByCreatedAtDesc();
    }
}
