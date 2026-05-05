package com.tutorlink.service.order;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tutorlink.common.constant.OrderStatus;
import com.tutorlink.common.constant.ResultCode;
import com.tutorlink.common.exception.BusinessException;
import com.tutorlink.common.util.SnowflakeIdUtil;
import com.tutorlink.dao.mapper.OrderMapper;
import com.tutorlink.dao.mapper.TrialLessonMapper;
import com.tutorlink.model.dto.order.TrialLessonRequest;
import com.tutorlink.model.entity.Order;
import com.tutorlink.model.entity.TrialLesson;
import com.tutorlink.service.chat.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrialLessonService {

    private final TrialLessonMapper trialLessonMapper;
    private final OrderMapper orderMapper;
    private final OrderStateMachine orderStateMachine;
    private final ChatService chatService;

    /**
     * 家长创建试课邀请
     */
    @Transactional(rollbackFor = Exception.class)
    public TrialLesson createTrial(Long parentUserId, TrialLessonRequest request) {
        Order order = orderMapper.selectById(request.getOrderId());
        if (order == null || !order.getParentUserId().equals(parentUserId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        if (order.getStatus() != OrderStatus.CONFIRMED.getCode()) {
            throw new BusinessException(ResultCode.ORDER_STATUS_INVALID, "当前订单状态不允许创建试课");
        }

        TrialLesson trial = new TrialLesson();
        trial.setId(SnowflakeIdUtil.nextId());
        trial.setOrderId(order.getId());
        trial.setParentUserId(parentUserId);
        trial.setTutorUserId(order.getTutorUserId());
        trial.setTrialDate(request.getTrialDate());
        trial.setTrialDuration(request.getTrialDuration());
        trial.setTrialPrice(request.getTrialPrice());
        trial.setTrialAddress(request.getTrialAddress());
        trial.setTrialMode(request.getTrialMode());
        trial.setStatus(1); // 待确认
        trialLessonMapper.insert(trial);

        // 发送试课卡片消息给老师
        chatService.sendMessage(parentUserId, order.getTutorUserId(), 4,
                String.format("{\"trialId\":%d,\"trialDate\":\"%s\",\"trialPrice\":%d,\"trialAddress\":\"%s\",\"trialMode\":%d}",
                        trial.getId(), trial.getTrialDate(), trial.getTrialPrice(),
                        trial.getTrialAddress(), trial.getTrialMode()));

        return trial;
    }

    /**
     * 老师确认试课
     */
    @Transactional(rollbackFor = Exception.class)
    public void confirmTrial(Long trialId, Long tutorUserId) {
        TrialLesson trial = trialLessonMapper.selectById(trialId);
        if (trial == null || !trial.getTutorUserId().equals(tutorUserId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        if (trial.getStatus() != 1) {
            throw new BusinessException(ResultCode.ORDER_STATUS_INVALID, "试课状态不允许确认");
        }

        trial.setStatus(2); // 已确认
        trialLessonMapper.updateById(trial);

        // 订单状态改为 TRIAL
        orderStateMachine.startTrial(trial.getOrderId());
    }

    /**
     * 家长评价试课结果
     */
    @Transactional(rollbackFor = Exception.class)
    public void evaluateTrial(Long trialId, Long parentUserId, Integer result, String feedback) {
        TrialLesson trial = trialLessonMapper.selectById(trialId);
        if (trial == null || !trial.getParentUserId().equals(parentUserId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        if (trial.getStatus() != 2) {
            throw new BusinessException(ResultCode.ORDER_STATUS_INVALID, "试课状态不允许评价");
        }

        trial.setStatus(3); // 已完成
        trial.setResult(result); // 1=通过 2=不通过
        trial.setParentFeedback(feedback);
        trialLessonMapper.updateById(trial);

        if (result == 1) {
            // 试课通过：TRIAL -> CONFIRMED（回到待付款）
            orderStateMachine.confirmTrialPass(trial.getOrderId(), parentUserId);
        } else {
            // 试课不通过：TRIAL -> CANCELLED
            orderStateMachine.confirmTrialFail(trial.getOrderId(), parentUserId);
        }
    }

    /**
     * 查询订单的试课记录
     */
    public List<TrialLesson> listByOrder(Long orderId) {
        return trialLessonMapper.selectList(
                new LambdaQueryWrapper<TrialLesson>()
                        .eq(TrialLesson::getOrderId, orderId)
                        .orderByDesc(TrialLesson::getCreateTime));
    }

    /**
     * 查询试课详情
     */
    public TrialLesson getById(Long trialId) {
        return trialLessonMapper.selectById(trialId);
    }
}
