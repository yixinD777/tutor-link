package com.tutorlink.service.order;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tutorlink.common.constant.OrderStatus;
import com.tutorlink.common.constant.ResultCode;
import com.tutorlink.common.exception.BusinessException;
import com.tutorlink.common.util.SnowflakeIdUtil;
import com.tutorlink.dao.mapper.CourseScheduleMapper;
import com.tutorlink.dao.mapper.LessonSessionMapper;
import com.tutorlink.dao.mapper.OrderMapper;
import com.tutorlink.model.dto.order.CourseScheduleRequest;
import com.tutorlink.model.entity.CourseSchedule;
import com.tutorlink.model.entity.LessonSession;
import com.tutorlink.model.entity.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseScheduleService {

    private final CourseScheduleMapper scheduleMapper;
    private final LessonSessionMapper sessionMapper;
    private final OrderMapper orderMapper;

    /**
     * 家长创建课程排期
     */
    @Transactional(rollbackFor = Exception.class)
    public List<CourseSchedule> createSchedules(Long parentUserId, CourseScheduleRequest request) {
        Order order = orderMapper.selectById(request.getOrderId());
        if (order == null || !order.getParentUserId().equals(parentUserId)) {
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
            schedule.setParentUserId(parentUserId);
            schedule.setTutorUserId(order.getTutorUserId());
            schedule.setDayOfWeek(item.getDayOfWeek());
            schedule.setStartTime(item.getStartTime());
            schedule.setEndTime(item.getEndTime());
            schedule.setTeachingAddress(item.getTeachingAddress());
            schedule.setTeachingMode(item.getTeachingMode());
            schedule.setHourlyRate(item.getHourlyRate());
            schedule.setEffectiveFrom(item.getEffectiveFrom());
            schedule.setEffectiveUntil(item.getEffectiveUntil());
            schedule.setStatus(1); // 生效
            scheduleMapper.insert(schedule);
            schedules.add(schedule);
        }

        return schedules;
    }

    /**
     * 查询订单的排期列表
     */
    public List<CourseSchedule> listByOrder(Long orderId) {
        return scheduleMapper.selectList(
                new LambdaQueryWrapper<CourseSchedule>()
                        .eq(CourseSchedule::getOrderId, orderId)
                        .eq(CourseSchedule::getStatus, 1)
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
        schedule.setStatus(2); // 暂停
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
        schedule.setStatus(1); // 生效
        scheduleMapper.updateById(schedule);
    }

    /**
     * 生成指定日期范围内的 lesson_session
     * 可由定时任务调用
     */
    @Transactional(rollbackFor = Exception.class)
    public int generateSessions(LocalDate from, LocalDate to) {
        List<CourseSchedule> activeSchedules = scheduleMapper.selectList(
                new LambdaQueryWrapper<CourseSchedule>()
                        .eq(CourseSchedule::getStatus, 1)
                        .le(CourseSchedule::getEffectiveFrom, to)
                        .and(w -> w.isNull(CourseSchedule::getEffectiveUntil)
                                .or().ge(CourseSchedule::getEffectiveUntil, from)));

        int count = 0;
        LocalDate date = from;
        while (!date.isAfter(to)) {
            int dow = date.getDayOfWeek().getValue(); // 1=Monday
            for (CourseSchedule schedule : activeSchedules) {
                if (schedule.getDayOfWeek() == dow) {
                    // 检查是否已存在
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
                        session.setStatus(1); // 待上课
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
}
