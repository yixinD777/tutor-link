package com.tutorlink.service.admin;

import com.tutorlink.dao.mapper.*;
import com.tutorlink.model.dto.admin.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminAnalyticsService {

    private final UserMapper userMapper;
    private final OrderMapper orderMapper;
    private final PaymentMapper paymentMapper;
    private final RefundMapper refundMapper;
    private final TutorProfileMapper tutorProfileMapper;
    private final ReviewMapper reviewMapper;

    public TrendDataResponse getUserTrend(TrendQueryRequest request) {
        List<Map<String, Object>> rows = userMapper.countByDate(
                request.getStartDate(), request.getEndDate(), request.getGranularity());
        return buildTrendData(rows, request);
    }

    public TrendDataResponse getOrderTrend(TrendQueryRequest request) {
        List<Map<String, Object>> rows = orderMapper.countByDateWithRevenue(
                request.getStartDate(), request.getEndDate(), request.getGranularity());
        return buildTrendDataWithAmount(rows, request);
    }

    public TrendDataResponse getRevenueTrend(TrendQueryRequest request) {
        List<Map<String, Object>> rows = paymentMapper.revenueByDate(
                request.getStartDate(), request.getEndDate(), request.getGranularity());
        List<String> labels = new ArrayList<>();
        List<Long> counts = new ArrayList<>();
        List<BigDecimal> amounts = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            labels.add(String.valueOf(row.get("date")));
            counts.add(0L);
            amounts.add(toBigDecimal(row.get("amount")));
        }
        return new TrendDataResponse(labels, counts, amounts);
    }

    public TrendDataResponse getRefundTrend(TrendQueryRequest request) {
        List<Map<String, Object>> rows = refundMapper.refundTrendByDate(
                request.getStartDate(), request.getEndDate(), request.getGranularity());
        return buildTrendDataWithAmount(rows, request);
    }

    public TrendDataResponse getReviewRatingTrend(TrendQueryRequest request) {
        List<Map<String, Object>> rows = reviewMapper.ratingTrendByDate(
                request.getStartDate(), request.getEndDate(), request.getGranularity());
        List<String> labels = new ArrayList<>();
        List<Long> counts = new ArrayList<>();
        List<BigDecimal> amounts = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            labels.add(String.valueOf(row.get("date")));
            counts.add(toLong(row.get("count")));
            amounts.add(toBigDecimal(row.get("avg_rating")));
        }
        return new TrendDataResponse(labels, counts, amounts);
    }

    public DistributionDataResponse getOrderStatusDistribution(DistributionQueryRequest request) {
        List<Map<String, Object>> rows = orderMapper.countByStatus();
        Map<Integer, String> statusNames = Map.of(
                1, "待确认", 2, "已确认", 3, "已支付", 4, "进行中",
                5, "已完成", 6, "已取消", 7, "退款中", 8, "已退款", 9, "争议中"
        );
        return buildDistribution(rows, statusNames, "status");
    }

    public DistributionDataResponse getSubjectPopularity(DistributionQueryRequest request) {
        List<Map<String, Object>> rows = orderMapper.countBySubject();
        return buildDistributionSimple(rows);
    }

    public DistributionDataResponse getTeachingModeDistribution(DistributionQueryRequest request) {
        List<Map<String, Object>> rows = orderMapper.countByTeachingMode();
        Map<Integer, String> modeNames = Map.of(1, "线上", 2, "线下", 3, "不限");
        return buildDistribution(rows, modeNames, "teaching_mode");
    }

    public DistributionDataResponse getGenderDistribution(DistributionQueryRequest request) {
        List<Map<String, Object>> rows = userMapper.countByGender();
        Map<Integer, String> genderNames = Map.of(0, "未知", 1, "男", 2, "女");
        return buildDistribution(rows, genderNames, "gender");
    }

    public DistributionDataResponse getRegionDistribution(DistributionQueryRequest request) {
        List<Map<String, Object>> rows = userMapper.countByProvince();
        return buildDistributionSimple(rows);
    }

    public DistributionDataResponse getTutorRatingDistribution(DistributionQueryRequest request) {
        List<Map<String, Object>> rows = tutorProfileMapper.countByRatingRange();
        return buildDistributionSimple(rows);
    }

    public DistributionDataResponse getUniversityDistribution(DistributionQueryRequest request) {
        List<Map<String, Object>> rows = tutorProfileMapper.countByUniversity(15);
        return buildDistributionSimple(rows);
    }

    public DistributionDataResponse getCertificationStatusDistribution(DistributionQueryRequest request) {
        List<Map<String, Object>> rows = tutorProfileMapper.countByCertificationStatus();
        Map<Integer, String> statusNames = Map.of(0, "未认证", 1, "审核中", 2, "已通过", 3, "已拒绝");
        return buildDistribution(rows, statusNames, "certification_status");
    }

    public DistributionDataResponse getHourlyRateDistribution(DistributionQueryRequest request) {
        List<Map<String, Object>> rows = tutorProfileMapper.countByHourlyRateRange();
        return buildDistributionSimple(rows);
    }

    public DistributionDataResponse getPaymentStatusDistribution(DistributionQueryRequest request) {
        List<Map<String, Object>> rows = paymentMapper.countByStatus();
        Map<Integer, String> statusNames = Map.of(1, "待支付", 2, "已支付", 3, "已释放", 4, "冻结中");
        return buildDistribution(rows, statusNames, "status");
    }

    public DistributionDataResponse getReviewRatingDistribution(DistributionQueryRequest request) {
        List<Map<String, Object>> rows = reviewMapper.countByRating();
        Map<Integer, String> ratingNames = new LinkedHashMap<>();
        for (int i = 1; i <= 5; i++) ratingNames.put(i, i + "星");
        return buildDistribution(rows, ratingNames, "rating");
    }

    private TrendDataResponse buildTrendData(List<Map<String, Object>> rows, TrendQueryRequest request) {
        List<String> labels = new ArrayList<>();
        List<Long> counts = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            labels.add(String.valueOf(row.get("date")));
            counts.add(toLong(row.get("count")));
        }
        return new TrendDataResponse(labels, counts, null);
    }

    private TrendDataResponse buildTrendDataWithAmount(List<Map<String, Object>> rows, TrendQueryRequest request) {
        List<String> labels = new ArrayList<>();
        List<Long> counts = new ArrayList<>();
        List<BigDecimal> amounts = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            labels.add(String.valueOf(row.get("date")));
            counts.add(toLong(row.get("count")));
            amounts.add(toBigDecimal(row.get("amount")));
        }
        return new TrendDataResponse(labels, counts, amounts);
    }

    private DistributionDataResponse buildDistribution(List<Map<String, Object>> rows,
                                                         Map<Integer, String> nameMap, String key) {
        long total = rows.stream().mapToLong(r -> toLong(r.get("count"))).sum();
        List<DistributionItemResponse> items = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            int val = ((Number) row.get(key)).intValue();
            long count = toLong(row.get("count"));
            String label = nameMap.getOrDefault(val, String.valueOf(val));
            BigDecimal pct = total > 0 ? BigDecimal.valueOf(count).multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(total), 1, RoundingMode.HALF_UP) : BigDecimal.ZERO;
            items.add(new DistributionItemResponse(label, count, pct));
        }
        return new DistributionDataResponse(items);
    }

    private DistributionDataResponse buildDistributionSimple(List<Map<String, Object>> rows) {
        long total = rows.stream().mapToLong(r -> toLong(r.get("count"))).sum();
        List<DistributionItemResponse> items = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            String label = String.valueOf(row.get("label"));
            long count = toLong(row.get("count"));
            BigDecimal pct = total > 0 ? BigDecimal.valueOf(count).multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(total), 1, RoundingMode.HALF_UP) : BigDecimal.ZERO;
            items.add(new DistributionItemResponse(label, count, pct));
        }
        return new DistributionDataResponse(items);
    }

    private long toLong(Object val) {
        if (val == null) return 0L;
        return ((Number) val).longValue();
    }

    private BigDecimal toBigDecimal(Object val) {
        if (val == null) return BigDecimal.ZERO;
        if (val instanceof BigDecimal) return (BigDecimal) val;
        return new BigDecimal(val.toString());
    }
}
