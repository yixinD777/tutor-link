package com.tutorlink.service.order;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tutorlink.common.constant.OrderStatus;
import com.tutorlink.common.constant.ResultCode;
import com.tutorlink.common.constant.ScheduleStatus;
import com.tutorlink.common.exception.BusinessException;
import com.tutorlink.common.util.SnowflakeIdUtil;
import com.tutorlink.dao.mapper.CourseScheduleMapper;
import com.tutorlink.dao.mapper.LessonSessionMapper;
import com.tutorlink.dao.mapper.OrderMapper;
import com.tutorlink.dao.mapper.UserMapper;
import com.tutorlink.model.dto.order.CourseScheduleRequest;
import com.tutorlink.model.dto.order.MyScheduleResponse;
import com.tutorlink.model.entity.CourseSchedule;
import com.tutorlink.model.entity.LessonSession;
import com.tutorlink.model.entity.Order;
import com.tutorlink.model.entity.User;
import com.tutorlink.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseScheduleService {

    private final CourseScheduleMapper scheduleMapper;
    private final LessonSessionMapper sessionMapper;
    private final OrderMapper orderMapper;
    private final UserMapper userMapper;
    private final NotificationService notificationService;

    /**
     * 创建课程排期（家长或家教均可发起，需对方确认）
     */
    @Transactional(rollbackFor = Exception.class)
    public List<CourseSchedule> createSchedules(Long userId, CourseScheduleRequest request) {
        Order order = orderMapper.selectById(request.getOrderId());
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        if (!order.getParentUserId().equals(userId) && !order.getTutorUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        if (order.getStatus() != OrderStatus.PAID.getCode()
                && order.getStatus() != OrderStatus.IN_PROGRESS.getCode()) {
            throw new BusinessException(ResultCode.ORDER_STATUS_INVALID, "当前订单状态不允许创建排期");
        }

        List<CourseSchedule> schedules = new ArrayList<>();
        for (CourseScheduleRequest.ScheduleItem item : request.getSchedules()) {
            CourseSchedule schedule = new CourseSchedule();
            schedule.setId(SnowflakeIdUtil.nextId());
            schedule.setOrderId(order.getId());
            schedule.setParentUserId(order.getParentUserId());
            schedule.setTutorUserId(order.getTutorUserId());
            schedule.setCreatedByUserId(userId);
            schedule.setDayOfWeek(item.getDayOfWeek());
            schedule.setStartTime(item.getStartTime());
            schedule.setEndTime(item.getEndTime());
            schedule.setTeachingAddress(item.getTeachingAddress());
            schedule.setTeachingMode(item.getTeachingMode());
            schedule.setHourlyRate(item.getHourlyRate());
            schedule.setEffectiveFrom(item.getEffectiveFrom());
            schedule.setEffectiveUntil(item.getEffectiveUntil());
            schedule.setStatus(ScheduleStatus.PENDING.getCode()); // 待确认
            scheduleMapper.insert(schedule);
            schedules.add(schedule);
        }

        // 通知对方
        Long counterpartyId = order.getParentUserId().equals(userId)
                ? order.getTutorUserId() : order.getParentUserId();
        User creator = userMapper.selectById(userId);
        String creatorName = creator != null ? creator.getNickname() : "用户";
        notificationService.sendNotification(counterpartyId, 6,
                "新排期提议", creatorName + " 提议了新的课程排期，请确认", order.getId());

        return schedules;
    }

    /**
     * 我的排期列表（按订单分组）
     */
    public List<MyScheduleResponse> listMySchedules(Long userId) {
        List<CourseSchedule> allSchedules = scheduleMapper.selectList(
                new LambdaQueryWrapper<CourseSchedule>()
                        .and(w -> w.eq(CourseSchedule::getParentUserId, userId)
                                .or().eq(CourseSchedule::getTutorUserId, userId))
                        .orderByDesc(CourseSchedule::getCreateTime));

        if (allSchedules.isEmpty()) {
            return Collections.emptyList();
        }

        // 按 orderId 分组
        Map<Long, List<CourseSchedule>> byOrder = allSchedules.stream()
                .collect(Collectors.groupingBy(CourseSchedule::getOrderId));

        List<MyScheduleResponse> result = new ArrayList<>();
        for (Map.Entry<Long, List<CourseSchedule>> entry : byOrder.entrySet()) {
            Long orderId = entry.getKey();
            List<CourseSchedule> schedules = entry.getValue();

            Order order = orderMapper.selectById(orderId);
            if (order == null) continue;

            CourseSchedule first = schedules.get(0);
            boolean isParent = order.getParentUserId().equals(userId);
            Long counterpartyId = isParent ? order.getTutorUserId() : order.getParentUserId();

            MyScheduleResponse resp = new MyScheduleResponse();
            resp.setOrderId(orderId);
            resp.setOrderTitle(order.getTitle());
            resp.setCounterpartyUserId(counterpartyId);

            User counterparty = userMapper.selectById(counterpartyId);
            if (counterparty != null) {
                resp.setCounterpartyNickname(counterparty.getNickname());
                resp.setCounterpartyAvatarUrl(counterparty.getAvatarUrl());
            }

            resp.setSchedules(schedules);
            result.add(resp);
        }

        return result;
    }

    /**
     * 确认排期
     */
    @Transactional(rollbackFor = Exception.class)
    public void confirmSchedule(Long scheduleId, Long userId) {
        CourseSchedule schedule = scheduleMapper.selectById(scheduleId);
        if (schedule == null) {
            throw new BusinessException(ResultCode.SCHEDULE_NOT_FOUND);
        }
        if (schedule.getStatus() != ScheduleStatus.PENDING.getCode()) {
            throw new BusinessException(ResultCode.SCHEDULE_STATUS_INVALID, "排期非待确认状态");
        }
        if (schedule.getCreatedByUserId().equals(userId)) {
            throw new BusinessException(ResultCode.SCHEDULE_CANNOT_CONFIRM_OWN);
        }
        if (!schedule.getParentUserId().equals(userId) && !schedule.getTutorUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        schedule.setStatus(ScheduleStatus.ACTIVE.getCode());
        scheduleMapper.updateById(schedule);

        // 通知创建者
        notificationService.sendNotification(schedule.getCreatedByUserId(), 7,
                "排期已确认", "您提议的课程排期已被确认", schedule.getOrderId());

        // 确认后立即生成近期课时
        LocalDate from = LocalDate.now();
        LocalDate to = from.plusWeeks(4);
        if (schedule.getEffectiveFrom() != null && schedule.getEffectiveFrom().isAfter(from)) {
            from = schedule.getEffectiveFrom();
        }
        if (schedule.getEffectiveUntil() != null && schedule.getEffectiveUntil().isBefore(to)) {
            to = schedule.getEffectiveUntil();
        }
        generateSessionsForSchedule(schedule, from, to);
    }

    /**
     * 拒绝排期
     */
    public void rejectSchedule(Long scheduleId, Long userId) {
        CourseSchedule schedule = scheduleMapper.selectById(scheduleId);
        if (schedule == null) {
            throw new BusinessException(ResultCode.SCHEDULE_NOT_FOUND);
        }
        if (schedule.getStatus() != ScheduleStatus.PENDING.getCode()) {
            throw new BusinessException(ResultCode.SCHEDULE_STATUS_INVALID, "排期非待确认状态");
        }
        if (schedule.getCreatedByUserId().equals(userId)) {
            throw new BusinessException(ResultCode.SCHEDULE_CANNOT_CONFIRM_OWN);
        }
        if (!schedule.getParentUserId().equals(userId) && !schedule.getTutorUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        scheduleMapper.deleteById(scheduleId); // 逻辑删除

        // 通知创建者
        notificationService.sendNotification(schedule.getCreatedByUserId(), 8,
                "排期已拒绝", "您提议的课程排期已被拒绝", schedule.getOrderId());
    }

    /**
     * 查询订单的排期列表（含待确认）
     */
    public List<CourseSchedule> listByOrder(Long orderId) {
        return scheduleMapper.selectList(
                new LambdaQueryWrapper<CourseSchedule>()
                        .eq(CourseSchedule::getOrderId, orderId)
                        .in(CourseSchedule::getStatus, Arrays.asList(
                                ScheduleStatus.PENDING.getCode(),
                                ScheduleStatus.ACTIVE.getCode(),
                                ScheduleStatus.PAUSED.getCode()))
                        .orderByAsc(CourseSchedule::getDayOfWeek)
                        .orderByAsc(CourseSchedule::getStartTime));
    }

    /**
     * 暂停排期
     */
    public void pauseSchedule(Long scheduleId, Long userId) {
        CourseSchedule schedule = scheduleMapper.selectById(scheduleId);
        if (schedule == null || (!schedule.getParentUserId().equals(userId) && !schedule.getTutorUserId().equals(userId))) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        if (schedule.getStatus() != ScheduleStatus.ACTIVE.getCode()) {
            throw new BusinessException(ResultCode.SCHEDULE_STATUS_INVALID, "只有生效中的排期可以暂停");
        }
        schedule.setStatus(ScheduleStatus.PAUSED.getCode());
        scheduleMapper.updateById(schedule);
    }

    /**
     * 恢复排期
     */
    public void resumeSchedule(Long scheduleId, Long userId) {
        CourseSchedule schedule = scheduleMapper.selectById(scheduleId);
        if (schedule == null || (!schedule.getParentUserId().equals(userId) && !schedule.getTutorUserId().equals(userId))) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        if (schedule.getStatus() != ScheduleStatus.PAUSED.getCode()) {
            throw new BusinessException(ResultCode.SCHEDULE_STATUS_INVALID, "只有暂停的排期可以恢复");
        }
        schedule.setStatus(ScheduleStatus.ACTIVE.getCode());
        scheduleMapper.updateById(schedule);
    }

    /**
     * 生成指定日期范围内的 lesson_session（定时任务调用）
     */
    @Transactional(rollbackFor = Exception.class)
    public int generateSessions(LocalDate from, LocalDate to) {
        List<CourseSchedule> activeSchedules = scheduleMapper.selectList(
                new LambdaQueryWrapper<CourseSchedule>()
                        .eq(CourseSchedule::getStatus, ScheduleStatus.ACTIVE.getCode())
                        .le(CourseSchedule::getEffectiveFrom, to)
                        .and(w -> w.isNull(CourseSchedule::getEffectiveUntil)
                                .or().ge(CourseSchedule::getEffectiveUntil, from)));

        int count = 0;
        LocalDate date = from;
        while (!date.isAfter(to)) {
            int dow = date.getDayOfWeek().getValue();
            for (CourseSchedule schedule : activeSchedules) {
                if (schedule.getDayOfWeek() == dow) {
                    Long existing = sessionMapper.selectCount(
                            new LambdaQueryWrapper<LessonSession>()
                                    .eq(LessonSession::getScheduleId, schedule.getId())
                                    .eq(LessonSession::getLessonDate, date));
                    if (existing == 0) {
                        LessonSession session = new LessonSession();
                        session.setId(SnowflakeIdUtil.nextId());
                        session.setScheduleId(schedule.getId());
                        session.setOrderId(schedule.getOrderId());
                        session.setLessonDate(date);
                        session.setStartTime(schedule.getStartTime());
                        session.setEndTime(schedule.getEndTime());
                        session.setStatus(1);
                        session.setParentConfirm(0);
                        sessionMapper.insert(session);
                        count++;
                    }
                }
            }
            date = date.plusDays(1);
        }
        return count;
    }

    /**
     * 为单个排期生成课时
     */
    private void generateSessionsForSchedule(CourseSchedule schedule, LocalDate from, LocalDate to) {
        LocalDate date = from;
        while (!date.isAfter(to)) {
            int dow = date.getDayOfWeek().getValue();
            if (schedule.getDayOfWeek() == dow) {
                Long existing = sessionMapper.selectCount(
                        new LambdaQueryWrapper<LessonSession>()
                                .eq(LessonSession::getScheduleId, schedule.getId())
                                .eq(LessonSession::getLessonDate, date));
                if (existing == 0) {
                    LessonSession session = new LessonSession();
                    session.setId(SnowflakeIdUtil.nextId());
                    session.setScheduleId(schedule.getId());
                    session.setOrderId(schedule.getOrderId());
                    session.setLessonDate(date);
                    session.setStartTime(schedule.getStartTime());
                    session.setEndTime(schedule.getEndTime());
                    session.setStatus(1);
                    session.setParentConfirm(0);
                    sessionMapper.insert(session);
                }
            }
            date = date.plusDays(1);
        }
    }
}
