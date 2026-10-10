package vn.btctu.daklak.kpi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.btctu.daklak.kpi.model.Task;
import vn.btctu.daklak.kpi.repository.TaskRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class TaskService {
    @Autowired
    private TaskRepository taskRepo;

    public List<Task> getAllTasks() {
        return taskRepo.findAll();
    }

    public List<Task> getTasksByUser(Long userId) {
        return taskRepo.findByAssignedToIdOrderByDueDateAsc(userId);
    }

    public List<Task> getTasksByDepartment(Long deptId) {
        return taskRepo.findByDepartmentIdOrderByDueDateAsc(deptId);
    }

    public Task saveTask(Task t) {
        if (t.getProgress() != null && t.getProgress() >= 100) {
            t.setStatus("COMPLETED");
            if (t.getCompletedDate() == null) {
                t.setCompletedDate(LocalDate.now());
            }
        } else if (t.getDueDate() != null && t.getDueDate().isBefore(LocalDate.now())) {
            t.setStatus("OVERDUE");
        }
        return taskRepo.save(t);
    }

    public Optional<Task> findById(Long id) {
        return taskRepo.findById(id);
    }
}
