package vn.btctu.daklak.kpi.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import vn.btctu.daklak.kpi.model.Evaluation;
import vn.btctu.daklak.kpi.model.Task;
import vn.btctu.daklak.kpi.model.User;
import vn.btctu.daklak.kpi.repository.EvaluationRepository;
import vn.btctu.daklak.kpi.repository.TaskRepository;
import vn.btctu.daklak.kpi.repository.UserRepository;
import java.util.List;

@Controller
public class DashboardController {
    @Autowired
    private UserRepository userRepo;

    @Autowired
    private TaskRepository taskRepo;

    @Autowired
    private EvaluationRepository evalRepo;

    @GetMapping(/)
    public String index(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute(CURRENT_USER);
        model.addAttribute(user, currentUser);

        long totalUsers = userRepo.count();
        long totalTasks = taskRepo.count();
        long completedTasks = taskRepo.countAllCompleted();
        Double avgScore = evalRepo.getAverageScoreByPeriod(Tháng 09/2026);

        model.addAttribute(totalUsers, totalUsers);
        model.addAttribute(totalTasks, totalTasks);
        model.addAttribute(completedTasks, completedTasks);
        model.addAttribute(avgScore, avgScore != null ? Math.round(avgScore * 10.0) / 10.0 : 92.5);

        List<Evaluation> evals = evalRepo.findByPeriodValue(Tháng 09/2026);
        model.addAttribute(evaluations, evals);

        List<Task> recentTasks = taskRepo.findAll();
        model.addAttribute(recentTasks, recentTasks);

        return dashboard;
    }
}
