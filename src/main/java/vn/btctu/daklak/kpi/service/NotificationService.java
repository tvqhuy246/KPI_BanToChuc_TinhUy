package vn.btctu.daklak.kpi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.btctu.daklak.kpi.model.Notification;
import vn.btctu.daklak.kpi.model.User;
import vn.btctu.daklak.kpi.repository.NotificationRepository;
import java.util.List;

@Service
public class NotificationService {
    @Autowired
    private NotificationRepository notifRepo;

    public Notification sendNotification(User recipient, User sender, String title, String content, String notifType, String refType, Long refId) {
        if (recipient == null) return null;
        Notification notif = new Notification(recipient, sender, title, content, notifType, refType, refId);
        return notifRepo.save(notif);
    }

    public List<Notification> getMyNotifications(Long userId) {
        return notifRepo.findByRecipientIdOrderByCreatedAtDesc(userId);
    }

    public List<Notification> getUnreadNotifications(Long userId) {
        return notifRepo.findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(userId);
    }

    public long countUnread(Long userId) {
        return notifRepo.countByRecipientIdAndIsReadFalse(userId);
    }

    public void markAsRead(Long notifId) {
        notifRepo.findById(notifId).ifPresent(n -> {
            n.setIsRead(true);
            notifRepo.save(n);
        });
    }

    public void markAllAsRead(Long userId) {
        List<Notification> list = notifRepo.findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        for (Notification n : list) {
            n.setIsRead(true);
        }
        notifRepo.saveAll(list);
    }
}
