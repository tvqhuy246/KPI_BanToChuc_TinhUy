package vn.btctu.daklak.kpi.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.btctu.daklak.kpi.model.Task;
import vn.btctu.daklak.kpi.model.User;
import vn.btctu.daklak.kpi.service.AuditLogService;
import vn.btctu.daklak.kpi.service.TaskService;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/tasks")
public class TaskController {
    @Autowired
    private TaskService taskService;

    @Autowired
    private AuditLogService auditLogService;

    @GetMapping
    public String listTasks(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("CURRENT_USER");
        model.addAttribute("user", currentUser);

        List<Task> tasks;
        if ("CHUYEN_VIEN".equals(currentUser.getRole())) {
            tasks = taskService.getTasksByUser(currentUser.getId());
        } else if ("TRUONG_PHONG".equals(currentUser.getRole())) {
            tasks = taskService.getTasksByDepartment(currentUser.getDepartment().getId());
        } else {
            tasks = taskService.getAllTasks();
        }
        model.addAttribute("tasks", tasks);
        return "tasks";
    }

    @PostMapping("/update-progress")
    public String updateProgress(@RequestParam("taskId") Long taskId,
                                 @RequestParam("progress") Integer progress,
                                 @RequestParam("evidenceText") String evidenceText,
                                 HttpSession session,
                                 HttpServletRequest request) {
        User currentUser = (User) session.getAttribute("CURRENT_USER");
        Optional<Task> tOpt = taskService.findById(taskId);
        if (tOpt.isPresent()) {
            Task t = tOpt.get();
            t.setProgress(progress);
            t.setEvidenceText(evidenceText);
            taskService.saveTask(t);
            auditLogService.log(currentUser.getId(), currentUser.getUsername(), "ACTION", "system", 1L, "Thao tac", request.getRemoteAddr());
        }
        return "redirect:/tasks";
    }
}
