package com.tutorlink.service.order;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tutorlink.common.constant.ResultCode;
import com.tutorlink.common.exception.BusinessException;
import com.tutorlink.dao.mapper.LessonSessionMapper;
import com.tutorlink.model.entity.LessonSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LessonSessionService {

    private final LessonSessionMapper sessionMapper;

    /**
     * 老师签到
     */
    @Transactional(rollbackFor = Exception.class)
    public void checkIn(Long sessionId, Long tutorUserId) {
        LessonSession session = sessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        if (session.getStatus() != 1 && session.getStatus() != 2) {
            throw new BusinessException(ResultCode.ORDER_STATUS_INVALID, "当前状态不允许签到");
        }

        session.setCheckInTime(LocalDateTime.now());
        session.setActualStart(LocalTime.now());
        session.setStatus(2); // 进行中
        sessionMapper.updateById(session);
    }

    /**
     * 老师签退
     */
    @Transactional(rollbackFor = Exception.class)
    public void checkOut(Long sessionId, Long tutorUserId) {
        LessonSession session = sessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        if (session.getStatus() != 2) {
            throw new BusinessException(ResultCode.ORDER_STATUS_INVALID, "当前状态不允许签退");
        }

        session.setCheckOutTime(LocalDateTime.now());
        session.setActualEnd(LocalTime.now());
        session.setStatus(3); // 已完成
        sessionMapper.updateById(session);
    }

    /**
     * 家长确认课时
     */
    @Transactional(rollbackFor = Exception.class)
    public void parentConfirm(Long sessionId, Long parentUserId) {
        LessonSession session = sessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        if (session.getStatus() != 3) {
            throw new BusinessException(ResultCode.ORDER_STATUS_INVALID, "课程未完成，无法确认");
        }

        session.setParentConfirm(1);
        sessionMapper.updateById(session);
    }

    /**
     * 标记缺席
     */
    public void markAbsent(Long sessionId, Long operatorUserId, String remark) {
        LessonSession session = sessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }

        session.setStatus(4); // 缺席
        session.setRemark(remark);
        sessionMapper.updateById(session);
    }

    /**
     * 查询订单的课时列表
     */
    public List<LessonSession> listByOrder(Long orderId) {
        return sessionMapper.selectList(
                new LambdaQueryWrapper<LessonSession>()
                        .eq(LessonSession::getOrderId, orderId)
                        .orderByAsc(LessonSession::getLessonDate)
                        .orderByAsc(LessonSession::getStartTime));
    }

    /**
     * 查询排期的课时列表
     */
    public List<LessonSession> listBySchedule(Long scheduleId) {
        return sessionMapper.selectList(
                new LambdaQueryWrapper<LessonSession>()
                        .eq(LessonSession::getScheduleId, scheduleId)
                        .orderByAsc(LessonSession::getLessonDate));
    }

    /**
     * 查询某天的课时
     */
    public List<LessonSession> listByDate(LocalDate date) {
        return sessionMapper.selectList(
                new LambdaQueryWrapper<LessonSession>()
                        .eq(LessonSession::getLessonDate, date)
                        .orderByAsc(LessonSession::getStartTime));
    }

    /**
     * 获取课时详情
     */
    public LessonSession getById(Long sessionId) {
        return sessionMapper.selectById(sessionId);
    }
}
