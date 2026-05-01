package com.tutorlink.service.notification;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tutorlink.dao.mapper.SystemNotificationMapper;
import com.tutorlink.model.entity.SystemNotification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SystemNotificationMapper notificationMapper;

    /**
     * 发送系统通知
     */
    public void sendNotification(Long userId, int type, String title, String content, Long relatedId) {
        SystemNotification notification = new SystemNotification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setRelatedId(relatedId);
        notification.setIsRead(0);
        notificationMapper.insert(notification);

        log.info("Notification sent: userId={}, type={}, title={}", userId, type, title);
        // TODO: 推送 WebSocket 通知
        // TODO: 推送微信订阅消息
    }

    /**
     * 获取用户通知列表
     */
    public List<SystemNotification> listNotifications(Long userId, int type) {
        LambdaQueryWrapper<SystemNotification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemNotification::getUserId, userId);
        if (type > 0) {
            wrapper.eq(SystemNotification::getType, type);
        }
        wrapper.orderByDesc(SystemNotification::getCreateTime);
        return notificationMapper.selectList(wrapper);
    }

    /**
     * 获取未读通知数量
     */
    public long getUnreadCount(Long userId) {
        return notificationMapper.selectCount(
                new LambdaQueryWrapper<SystemNotification>()
                        .eq(SystemNotification::getUserId, userId)
                        .eq(SystemNotification::getIsRead, 0));
    }

    /**
     * 标记通知已读
     */
    public void markAsRead(Long notificationId, Long userId) {
        SystemNotification notification = notificationMapper.selectById(notificationId);
        if (notification != null && notification.getUserId().equals(userId)) {
            notification.setIsRead(1);
            notificationMapper.updateById(notification);
        }
    }

    /**
     * 标记所有通知已读
     */
    public void markAllAsRead(Long userId) {
        List<SystemNotification> unread = notificationMapper.selectList(
                new LambdaQueryWrapper<SystemNotification>()
                        .eq(SystemNotification::getUserId, userId)
                        .eq(SystemNotification::getIsRead, 0));
        for (SystemNotification n : unread) {
            n.setIsRead(1);
            notificationMapper.updateById(n);
        }
    }
}
