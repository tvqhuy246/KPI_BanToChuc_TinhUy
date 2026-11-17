package vn.btctu.daklak.kpi.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.btctu.daklak.kpi.model.EvidenceFile;
import vn.btctu.daklak.kpi.model.Task;
import vn.btctu.daklak.kpi.model.User;
import vn.btctu.daklak.kpi.repository.EvidenceFileRepository;
import vn.btctu.daklak.kpi.repository.TaskRepository;
import vn.btctu.daklak.kpi.service.AuditLogService;

import java.io.InputStream;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller Quản lý Upload Minh chứng và Tính mã băm SHA-256
 * Căn cứ: Luật Dữ liệu và Nghị định số 165/2025/NĐ-CP
 */
@Controller
@RequestMapping("/evidence")
public class EvidenceFileController {

    @Autowired
    private EvidenceFileRepository evidenceRepo;

    @Autowired
    private TaskRepository taskRepo;

    @Autowired
    private AuditLogService auditLogService;

    @PostMapping("/upload")
    @ResponseBody
    public ResponseEntity<?> uploadEvidence(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "taskId", required = false) Long taskId,
            @RequestParam(value = "securityClassification", defaultValue = "NOI_BO") String securityClassification,
            HttpSession session,
            HttpServletRequest request) {

        User currentUser = (User) session.getAttribute("CURRENT_USER");
        if (currentUser == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Chưa đăng nhập"));
        }

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Vui lòng chọn tệp đính kèm"));
        }

        try {
            // TÍNH MÃ BĂM SHA-256 ĐẢM BẢO TÍNH TOÀN VẸN
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream is = file.getInputStream()) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = is.read(buffer)) > 0) {
                    digest.update(buffer, 0, read);
                }
            }
            byte[] hashBytes = digest.digest();
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            String sha256Hex = hexString.toString().toUpperCase();

            Task task = (taskId != null) ? taskRepo.findById(taskId).orElse(null) : null;
            String originalFilename = file.getOriginalFilename();
            String virtualPath = "/uploads/evidence/" + System.currentTimeMillis() + "_" + originalFilename;

            EvidenceFile evFile = new EvidenceFile(
                    task,
                    null,
                    currentUser,
                    originalFilename,
                    virtualPath,
                    file.getSize(),
                    file.getContentType(),
                    sha256Hex,
                    securityClassification
            );

            evidenceRepo.save(evFile);

            // Ghi vết an toàn bảo mật Cấp độ 3
            auditLogService.logSecurity(
                    currentUser.getId(),
                    currentUser.getUsername(),
                    "UPLOAD_EVIDENCE",
                    "evidence_files",
                    evFile.getId(),
                    "Đã tải tệp minh chứng: " + originalFilename + " | SHA-256: " + sha256Hex,
                    request.getRemoteAddr(),
                    request.getHeader("User-Agent"),
                    securityClassification
            );

            Map<String, Object> resp = new HashMap<>();
            resp.put("status", "SUCCESS");
            resp.put("fileId", evFile.getId());
            resp.put("fileName", originalFilename);
            resp.put("sha256Hash", sha256Hex);
            resp.put("message", "Tải lên tệp minh chứng và xác thực mã băm SHA-256 thành công");

            return ResponseEntity.ok(resp);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/by-task/{taskId}")
    @ResponseBody
    public ResponseEntity<List<EvidenceFile>> getByTaskId(@PathVariable Long taskId) {
        return ResponseEntity.ok(evidenceRepo.findByTaskId(taskId));
    }
}
