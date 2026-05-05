package com.tutorlink.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tutorlink.model.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    @Select("<script>" +
            "SELECT DATE_FORMAT(create_time, " +
            "<choose>" +
            "<when test=\"granularity == 'week'\">'%x-W%v'</when>" +
            "<when test=\"granularity == 'month'\">'%Y-%m'</when>" +
            "<otherwise>'%Y-%m-%d'</otherwise>" +
            "</choose>" +
            ") as `date`, COUNT(*) as `count`, COALESCE(SUM(total_amount), 0) as amount " +
            "FROM order_main WHERE is_deleted = 0 " +
            "<if test=\"startDate != null\">AND create_time &gt;= #{startDate}</if> " +
            "<if test=\"endDate != null\">AND create_time &lt;= #{endDate}</if> " +
            "GROUP BY `date` ORDER BY `date`" +
            "</script>")
    List<Map<String, Object>> countByDateWithRevenue(@Param("startDate") LocalDateTime startDate,
                                                      @Param("endDate") LocalDateTime endDate,
                                                      @Param("granularity") String granularity);

    @Select("SELECT status, COUNT(*) as `count` FROM order_main WHERE is_deleted = 0 GROUP BY status")
    List<Map<String, Object>> countByStatus();

    @Select("SELECT COALESCE(s.name, '未知') as label, COUNT(*) as `count` " +
            "FROM order_main o LEFT JOIN subject s ON o.subject_id = s.id AND s.is_deleted = 0 " +
            "WHERE o.is_deleted = 0 GROUP BY label ORDER BY `count` DESC LIMIT 15")
    List<Map<String, Object>> countBySubject();

    @Select("SELECT teaching_mode, COUNT(*) as `count` FROM order_main WHERE is_deleted = 0 GROUP BY teaching_mode")
    List<Map<String, Object>> countByTeachingMode();

    @Select("<script>" +
            "SELECT DATE_FORMAT(paid_time, " +
            "<choose>" +
            "<when test=\"granularity == 'week'\">'%x-W%v'</when>" +
            "<when test=\"granularity == 'month'\">'%Y-%m'</when>" +
            "<otherwise>'%Y-%m-%d'</otherwise>" +
            "</choose>" +
            ") as `date`, COALESCE(SUM(total_amount), 0) as amount " +
            "FROM order_main WHERE is_deleted = 0 AND status &gt;= 3 " +
            "<if test=\"startDate != null\">AND paid_time &gt;= #{startDate}</if> " +
            "<if test=\"endDate != null\">AND paid_time &lt;= #{endDate}</if> " +
            "GROUP BY `date` ORDER BY `date`" +
            "</script>")
    List<Map<String, Object>> revenueByDate(@Param("startDate") LocalDateTime startDate,
                                             @Param("endDate") LocalDateTime endDate,
                                             @Param("granularity") String granularity);
}
