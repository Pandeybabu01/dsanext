package com.dsanext.service;

import com.dsanext.domain.entity.Notification;
import com.dsanext.domain.entity.User;
import com.dsanext.domain.enums.NotificationType;
import com.dsanext.dto.common.PageResponse;
import com.dsanext.dto.response.NotificationResponse;
import com.dsanext.exception.ResourceNotFoundException;
import com.dsanext.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    // ── User-facing ──────────────────────────────────────────

    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> getUserNotifications(UUID userId, Pageable pageable) {
        return PageResponse.from(
                notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                        .map(NotificationResponse::from)
        );
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(UUID userId) {
        return notificationRepository.countByUserIdAndIsRead(userId, false);
    }

    @Transactional
    public void markAsRead(UUID notificationId, UUID userId) {
        int updated = notificationRepository.markAsRead(notificationId, userId);
        if (updated == 0) {
            throw new ResourceNotFoundException("Notification", "id", notificationId);
        }
    }

    @Transactional
    public int markAllAsRead(UUID userId) {
        return notificationRepository.markAllAsRead(userId);
    }

    @Transactional
    public int clearReadNotifications(UUID userId) {
        return notificationRepository.deleteReadByUserId(userId);
    }

    // ── Internal sending (async) ─────────────────────────────

    /**
     * Send a notification to a specific user asynchronously.
     * Safe to call from any service — never blocks the caller.
     */
    @Async("taskExecutor")
    @Transactional
    public void sendNotification(User user, String title,
            String message, NotificationType type) {
        try {
            Notification notification = Notification.builder()
                    .user(user)
                    .title(title)
                    .message(message)
                    .type(type)
                    .isRead(false)
                    .build();
            notificationRepository.save(notification);
            log.debug("Notification sent to user {}: {}", user.getId(), title);
        } catch (Exception ex) {
            log.error("Failed to send notification to user {}: {}", user.getId(), ex.getMessage());
        }
    }

    @Async("taskExecutor")
    @Transactional
    public void sendSystemNotification(User user, String title, String message) {
        sendNotification(user, title, message, NotificationType.SYSTEM);
    }
}
