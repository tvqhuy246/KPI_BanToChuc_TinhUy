package vn.btctu.daklak.kpi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.btctu.daklak.kpi.model.Evaluation;
import vn.btctu.daklak.kpi.model.User;
import vn.btctu.daklak.kpi.repository.EvaluationRepository;
import vn.btctu.daklak.kpi.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class ApprovalService {
    @Autowired
    private EvaluationRepository evalRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private NotificationService notifService;

    /**
     * Định tuyến thông minh (Smart Routing) khi nộp phiếu đánh giá
     * Tự động xác định người duyệt trực tiếp (manager_id) và gửi thông báo
     */
    public Evaluation submitWithSmartRouting(Evaluation eval, User currentUser) {
        User manager = currentUser.getManager();
        eval.setUser(currentUser);
        eval.setDepartment(currentUser.getDepartment());

        // Nếu cán bộ chưa có người quản lý trực tiếp, tự động tìm lãnh đạo phòng hoặc lãnh đạo ban
        if (manager == null && currentUser.getDepartment() != null) {
            List<User> deptUsers = userRepo.findByDepartmentId(currentUser.getDepartment().getId());
            for (User u : deptUsers) {
                if ("PHO_TRUONG_PHONG".equals(u.getRole())) {
                    manager = u;
                    break;
                }
            }
            if (manager == null) {
                for (User u : deptUsers) {
                    if ("TRUONG_PHONG".equals(u.getRole())) {
                        manager = u;
                        break;
                    }
                }
            }
            if (manager != null) {
                currentUser.setManager(manager);
                userRepo.save(currentUser);
            }
        }

        if (manager != null) {
            eval.setCurrentApprover(manager);
            String role = manager.getRole();
            if ("PHO_TRUONG_PHONG".equals(role)) {
                eval.setStatus("PENDING_DEPUTY_HEAD");
                eval.setCurrentStep(1);
            } else if ("TRUONG_PHONG".equals(role)) {
                eval.setStatus("PENDING_DEPT_HEAD");
                eval.setCurrentStep(2);
            } else if ("PHO_TRUONG_BAN".equals(role)) {
                eval.setStatus("PENDING_DEPUTY_LEADER");
                eval.setCurrentStep(3);
            } else if ("LANH_DAO_BAN".equals(role)) {
                eval.setStatus("PENDING_LEADER");
                eval.setCurrentStep(4);
            } else {
                eval.setStatus("SUBMITTED");
            }

            // Gửi thông báo thông minh cho Quản lý trực tiếp
            notifService.sendNotification(
                    manager,
                    currentUser,
                    "Phiếu đánh giá KPI mới cần duyệt (" + eval.getPeriodValue() + ")",
                    "Đồng chí " + currentUser.getFullName() + " (" + currentUser.getRoleBadge() + ") đã nộp phiếu tự đánh giá " + eval.getPeriodValue() + " (Tự chấm: " + eval.getSelfScore() + " điểm).",
                    "EVAL_SUBMITTED",
                    "EVALUATION",
                    eval.getId()
            );
        } else {
            // Không có manager (vd: Trưởng ban) -> Chuyển thẳng sang SUBMITTED
            eval.setStatus("SUBMITTED");
        }

        return evalRepo.save(eval);
    }

    /**
     * 1. Phó Trưởng phòng thẩm định điểm (Cấp 4 duyệt cho Cấp 5)
     */
    public Evaluation deputyHeadReview(Long evalId, User deputyHead, Double score, String notes) {
        Optional<Evaluation> eOpt = evalRepo.findById(evalId);
        if (eOpt.isPresent()) {
            Evaluation e = eOpt.get();
            e.setDeputyHeadScore(score);
            e.setDeputyHeadNotes(notes);
            e.setManagerScore(score);

            // Chuyển tiếp lên Trưởng phòng quản lý của Phó phòng
            User deptHead = deputyHead.getManager();
            if (deptHead == null && e.getDepartment() != null) {
                for (User u : userRepo.findByDepartmentId(e.getDepartment().getId())) {
                    if ("TRUONG_PHONG".equals(u.getRole())) {
                        deptHead = u;
                        break;
                    }
                }
            }
            if (deptHead != null) {
                e.setCurrentApprover(deptHead);
                e.setStatus("PENDING_DEPT_HEAD");
                e.setCurrentStep(2);
                notifService.sendNotification(
                        deptHead,
                        deputyHead,
                        "Hồ sơ KPI đã qua thẩm định của Phó phòng",
                        "Phó phòng " + deputyHead.getFullName() + " đã thẩm định phiếu KPI của " + e.getUser().getFullName() + " (" + score + " điểm). Kính chuyển Trưởng phòng xem xét.",
                        "EVAL_REVIEWED",
                        "EVALUATION",
                        e.getId()
                );
            } else {
                e.setStatus("PENDING_DEPT_HEAD");
                e.setCurrentStep(2);
            }
            return evalRepo.save(e);
        }
        return null;
    }

    /**
     * 2. Trưởng phòng thẩm định điểm (Cấp 3 duyệt)
     */
    public Evaluation deptHeadReview(Long evalId, User deptHead, Double score, String notes) {
        Optional<Evaluation> eOpt = evalRepo.findById(evalId);
        if (eOpt.isPresent()) {
            Evaluation e = eOpt.get();
            e.setDeptHeadScore(score);
            e.setDeptHeadNotes(notes);
            e.setManagerScore(score);

            // Chuyển tiếp lên Phó Trưởng ban phụ trách
            User deputyLeader = deptHead.getManager();
            if (deputyLeader == null && e.getDepartment() != null) {
                for (User u : userRepo.findByDepartmentId(e.getDepartment().getId())) {
                    if ("PHO_TRUONG_BAN".equals(u.getRole())) {
                        deputyLeader = u;
                        break;
                    }
                }
            }
            if (deputyLeader == null) {
                for (User u : userRepo.findAllActiveUsers()) {
                    if ("PHO_TRUONG_BAN".equals(u.getRole())) {
                        deputyLeader = u;
                        break;
                    }
                }
            }
            if (deputyLeader != null) {
                e.setCurrentApprover(deputyLeader);
                e.setStatus("PENDING_DEPUTY_LEADER");
                e.setCurrentStep(3);
                notifService.sendNotification(
                        deputyLeader,
                        deptHead,
                        "Hồ sơ KPI đã qua thẩm định của Trưởng phòng",
                        "Trưởng phòng " + deptHead.getFullName() + " đã đánh giá phiếu KPI của " + e.getUser().getFullName() + " (" + score + " điểm). Kính trình Phó Trưởng ban chỉ đạo.",
                        "EVAL_REVIEWED",
                        "EVALUATION",
                        e.getId()
                );
            } else {
                e.setStatus("PENDING_DEPUTY_LEADER");
                e.setCurrentStep(3);
            }
            return evalRepo.save(e);
        }
        return null;
    }

    /**
     * 3. Phó Trưởng ban thẩm định & cho ý kiến (Cấp 2 duyệt)
     */
    public Evaluation deputyLeaderReview(Long evalId, User deputyLeader, Double score, String notes) {
        Optional<Evaluation> eOpt = evalRepo.findById(evalId);
        if (eOpt.isPresent()) {
            Evaluation e = eOpt.get();
            e.setDeputyLeaderScore(score);
            e.setDeputyLeaderNotes(notes);
            e.setManagerScore(score);

            // Chuyển tiếp lên Trưởng ban
            User topLeader = deputyLeader.getManager();
            if (topLeader == null) {
                for (User u : userRepo.findAllActiveUsers()) {
                    if ("LANH_DAO_BAN".equals(u.getRole())) {
                        topLeader = u;
                        break;
                    }
                }
            }
            if (topLeader != null) {
                e.setCurrentApprover(topLeader);
                e.setStatus("PENDING_LEADER");
                e.setCurrentStep(4);
                notifService.sendNotification(
                        topLeader,
                        deputyLeader,
                        "Trình Trưởng ban phê duyệt kết quả KPI",
                        "Phó Trưởng ban " + deputyLeader.getFullName() + " kính trình kết quả đánh giá của " + e.getUser().getFullName() + " (" + score + " điểm) để Trưởng ban quyết định.",
                        "EVAL_REVIEWED",
                        "EVALUATION",
                        e.getId()
                );
            } else {
                e.setStatus("PENDING_LEADER");
                e.setCurrentStep(4);
            }
            return evalRepo.save(e);
        }
        return null;
    }

    /**
     * 4. Trưởng ban kết luận & Phê duyệt chính thức (Cấp 1 duyệt)
     * TÍCH HỢP TRẦN KHỐNG CHẾ XẾP LOẠI TỰ ĐỘNG (HD 05-HD/TU & QĐ 39-QĐ/TU):
     * Tỷ lệ cán bộ "Hoàn thành xuất sắc nhiệm vụ" không vượt quá 20% tổng số cán bộ trong kỳ.
     */
    public Evaluation topLeaderApprove(Long evalId, User leader, Double finalScore, String ranking, String leaderNotes) {
        Optional<Evaluation> eOpt = evalRepo.findById(evalId);
        if (eOpt.isPresent()) {
            Evaluation e = eOpt.get();

            // KIỂM SOÁT TRẦN KHỐNG CHẾ 20% XUẤT SẮC NHIỆM VỤ
            String adjustedRanking = ranking;
            if ("XUAT_SAC".equals(ranking)) {
                String period = e.getPeriodValue();
                List<Evaluation> allPeriodEvals = evalRepo.findByPeriodValue(period);
                long totalOfficers = userRepo.count();
                if (totalOfficers == 0) totalOfficers = 39;
                
                long maxExcellentAllowed = Math.max(1, Math.round(totalOfficers * 0.20)); // Tối đa 20% (vd: 39 cán bộ -> tối đa 8 xuất sắc)
                long currentExcellentCount = allPeriodEvals.stream()
                        .filter(ev -> "XUAT_SAC".equals(ev.getRanking()) && !ev.getId().equals(evalId))
                        .count();

                if (currentExcellentCount >= maxExcellentAllowed) {
                    // Đã chạm hoặc vượt trần 20% -> Tự động khống chế về Hoàn thành Tốt kèm ghi chú cảnh báo quy định
                    adjustedRanking = "TOT";
                    String warning = "[KHỐNG CHẾ TRẦN 20% - QĐ 366-QĐ/TW & NĐ 90/2020/NĐ-CP]: Số lượng Hoàn thành Xuất sắc đã đạt trần 20% (" 
                            + maxExcellentAllowed + "/" + totalOfficers + " cán bộ). Hồ sơ tự động xếp loại 'Hoàn thành Tốt'. ";
                    leaderNotes = (leaderNotes != null && !leaderNotes.isEmpty()) ? warning + leaderNotes : warning;
                }
            }

            e.setApprovedBy(leader);
            e.setFinalScore(finalScore);
            e.setRanking(adjustedRanking);
            e.setLeaderNotes(leaderNotes);
            e.setStatus("APPROVED");
            e.setApprovedAt(LocalDateTime.now());
            e.setCurrentApprover(null);

            // Báo kết quả về cho chính cán bộ được đánh giá
            notifService.sendNotification(
                    e.getUser(),
                    leader,
                    "Kết quả đánh giá KPI chính thức (" + e.getPeriodValue() + ")",
                    "Đồng chí đã được Lãnh đạo Ban phê duyệt kết quả: " + finalScore + " điểm - Xếp loại: " + e.getRankingDisplayName(),
                    "EVAL_APPROVED",
                    "EVALUATION",
                    e.getId()
            );

            return evalRepo.save(e);
        }
        return null;
    }

    public Evaluation managerReview(Long evalId, Double managerScore, String managerNotes) {
        Optional<Evaluation> eOpt = evalRepo.findById(evalId);
        if (eOpt.isPresent()) {
            Evaluation e = eOpt.get();
            e.setDeptHeadScore(managerScore);
            e.setDeptHeadNotes(managerNotes);
            e.setManagerScore(managerScore);
            e.setFinalScore(managerScore);
            e.setManagerNotes(managerNotes);
            e.setStatus("REVIEWED");
            return evalRepo.save(e);
        }
        return null;
    }

    public Evaluation leaderApprove(Long evalId, User approver, Double finalScore, String ranking, String leaderNotes) {
        return topLeaderApprove(evalId, approver, finalScore, ranking, leaderNotes);
    }

    /**
     * Danh sách phiếu cần duyệt được định tuyến cho người dùng hiện tại
     */
    /**
     * Danh sách phiếu cần thẩm định / phê duyệt phân cấp theo đúng trình tự 5 cấp:
     * Cấp 5 (Chuyên viên) -> Cấp 4 (Phó phòng: PENDING_DEPUTY_HEAD)
     * -> Cấp 3 (Trưởng phòng: PENDING_DEPT_HEAD)
     * -> Cấp 2 (Phó Trưởng ban: PENDING_DEPUTY_LEADER)
     * -> Cấp 1 (Trưởng ban: PENDING_LEADER -> APPROVED).
     */
    public List<Evaluation> getPendingApprovalsForUser(User currentUser) {
        if (currentUser == null) return Collections.emptyList();

        String role = currentUser.getRole();
        int userLevel = currentUser.getOrgLevel() != null ? currentUser.getOrgLevel() : 5;
        List<Evaluation> allPending = evalRepo.findByStatusNotOrderByEvaluatedAtDesc("APPROVED");

        // 1. Quản trị viên hệ thống & Trưởng ban (Cấp 1):
        // Xem và phê duyệt tất cả hồ sơ đang chờ trong toàn cơ quan (trừ DRAFT)
        if ("ADMIN".equals(role) || "LANH_DAO_BAN".equals(role) || userLevel == 1) {
            return allPending.stream()
                    .filter(e -> !"DRAFT".equals(e.getStatus()))
                    .toList();
        }

        // 2. Phó Trưởng ban (Cấp 2):
        // Xem các hồ sơ của Cấp 3, 4, 5 thuộc khối/phòng phụ trách chưa chuyển lên Cấp 1 (trạng thái chưa phải PENDING_LEADER)
        if ("PHO_TRUONG_BAN".equals(role) || userLevel == 2) {
            Long deptId = currentUser.getDepartment() != null ? currentUser.getDepartment().getId() : null;
            return allPending.stream()
                    .filter(e -> e.getUser() != null && !e.getUser().getId().equals(currentUser.getId()))
                    .filter(e -> {
                        int evalUserLvl = e.getUser().getOrgLevel() != null ? e.getUser().getOrgLevel() : 5;
                        return evalUserLvl > 2;
                    })
                    .filter(e -> {
                        String st = e.getStatus();
                        return "SUBMITTED".equals(st) || "PENDING_DEPUTY_HEAD".equals(st)
                                || "PENDING_DEPT_HEAD".equals(st) || "PENDING_DEPUTY_LEADER".equals(st);
                    })
                    .filter(e -> {
                        if (e.getUser() != null && KpiCalculationService.isHuyTestAccount(e.getUser())) return true;
                        // Phó Trưởng ban thường trực (deptId == 1) xem toàn cơ quan; Phó ban phụ trách khối xem theo phòng hoặc được điều hướng trực tiếp
                        if (deptId == null || deptId == 1L) return true;
                        if (e.getCurrentApprover() != null && e.getCurrentApprover().getId().equals(currentUser.getId())) return true;
                        return e.getDepartment() != null && deptId.equals(e.getDepartment().getId());
                    })
                    .toList();
        }

        // 3. Trưởng phòng (Cấp 3):
        // Xem các hồ sơ của Cấp 4, 5 trong phòng mình đang ở bước Phó phòng hoặc Trưởng phòng (SUBMITTED, PENDING_DEPUTY_HEAD, PENDING_DEPT_HEAD)
        if ("TRUONG_PHONG".equals(role) || userLevel == 3) {
            Long deptId = currentUser.getDepartment() != null ? currentUser.getDepartment().getId() : null;
            return allPending.stream()
                    .filter(e -> e.getUser() != null && !e.getUser().getId().equals(currentUser.getId()))
                    .filter(e -> {
                        int evalUserLvl = e.getUser().getOrgLevel() != null ? e.getUser().getOrgLevel() : 5;
                        return evalUserLvl > 3;
                    })
                    .filter(e -> {
                        String st = e.getStatus();
                        return "SUBMITTED".equals(st) || "PENDING_DEPUTY_HEAD".equals(st) || "PENDING_DEPT_HEAD".equals(st);
                    })
                    .filter(e -> {
                        if (e.getUser() != null && KpiCalculationService.isHuyTestAccount(e.getUser())) return true;
                        if (e.getCurrentApprover() != null && e.getCurrentApprover().getId().equals(currentUser.getId())) return true;
                        return deptId != null && e.getDepartment() != null && deptId.equals(e.getDepartment().getId());
                    })
                    .toList();
        }

        // 4. Phó Trưởng phòng (Cấp 4):
        // Xem các hồ sơ của Chuyên viên Cấp 5 trong phòng mình đang chờ Phó phòng thẩm định (SUBMITTED, PENDING_DEPUTY_HEAD)
        if ("PHO_TRUONG_PHONG".equals(role) || userLevel == 4) {
            Long deptId = currentUser.getDepartment() != null ? currentUser.getDepartment().getId() : null;
            return allPending.stream()
                    .filter(e -> e.getUser() != null && !e.getUser().getId().equals(currentUser.getId()))
                    .filter(e -> {
                        int evalUserLvl = e.getUser().getOrgLevel() != null ? e.getUser().getOrgLevel() : 5;
                        return evalUserLvl > 4;
                    })
                    .filter(e -> {
                        String st = e.getStatus();
                        return "SUBMITTED".equals(st) || "PENDING_DEPUTY_HEAD".equals(st);
                    })
                    .filter(e -> {
                        if (e.getUser() != null && KpiCalculationService.isHuyTestAccount(e.getUser())) return true;
                        if (e.getCurrentApprover() != null && e.getCurrentApprover().getId().equals(currentUser.getId())) return true;
                        return deptId != null && e.getDepartment() != null && deptId.equals(e.getDepartment().getId());
                    })
                    .toList();
        }

        return Collections.emptyList();
    }
}
