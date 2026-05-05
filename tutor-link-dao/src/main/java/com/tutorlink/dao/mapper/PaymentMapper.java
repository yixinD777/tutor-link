package com.tutorlink.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tutorlink.model.entity.Payment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface PaymentMapper extends BaseMapper<Payment> {

    @Select("<script>" +
            "SELECT DATE_FORMAT(paid_time, " +
            "<choose>" +
            "<when test=\"granularity == 'week'\">'%x-W%v'</when>" +
            "<when test=\"granularity == 'month'\">'%Y-%m'</when>" +
            "<otherwise>'%Y-%m-%d'</otherwise>" +
            "</choose>" +
            ") as `date`, COALESCE(SUM(amount), 0) as amount, COALESCE(SUM(platform_fee), 0) as fee " +
            "FROM payment WHERE is_deleted = 0 AND status &gt;= 2 " +
            "<if test=\"startDate != null\">AND paid_time &gt;= #{startDate}</if> " +
            "<if test=\"endDate != null\">AND paid_time &lt;= #{endDate}</if> " +
            "GROUP BY `date` ORDER BY `date`" +
            "</script>")
    List<Map<String, Object>> revenueByDate(@Param("startDate") LocalDateTime startDate,
                                             @Param("endDate") LocalDateTime endDate,
                                             @Param("granularity") String granularity);

    @Select("SELECT status, COUNT(*) as `count` FROM payment WHERE is_deleted = 0 GROUP BY status")
    List<Map<String, Object>> countByStatus();
}
