package com.tutorlink.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tutorlink.model.entity.Refund;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface RefundMapper extends BaseMapper<Refund> {

    @Select("<script>" +
            "SELECT DATE_FORMAT(create_time, " +
            "<choose>" +
            "<when test=\"granularity == 'week'\">'%x-W%v'</when>" +
            "<when test=\"granularity == 'month'\">'%Y-%m'</when>" +
            "<otherwise>'%Y-%m-%d'</otherwise>" +
            "</choose>" +
            ") as `date`, COUNT(*) as `count`, COALESCE(SUM(amount), 0) as amount " +
            "FROM refund WHERE is_deleted = 0 " +
            "<if test=\"startDate != null\">AND create_time &gt;= #{startDate}</if> " +
            "<if test=\"endDate != null\">AND create_time &lt;= #{endDate}</if> " +
            "GROUP BY `date` ORDER BY `date`" +
            "</script>")
    List<Map<String, Object>> refundTrendByDate(@Param("startDate") LocalDateTime startDate,
                                                 @Param("endDate") LocalDateTime endDate,
                                                 @Param("granularity") String granularity);
}
