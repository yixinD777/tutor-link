package com.tutorlink.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tutorlink.model.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("<script>" +
            "SELECT DATE_FORMAT(create_time, " +
            "<choose>" +
            "<when test=\"granularity == 'week'\">'%x-W%v'</when>" +
            "<when test=\"granularity == 'month'\">'%Y-%m'</when>" +
            "<otherwise>'%Y-%m-%d'</otherwise>" +
            "</choose>" +
            ") as `date`, COUNT(*) as `count` " +
            "FROM user WHERE is_deleted = 0 " +
            "<if test=\"startDate != null\">AND create_time &gt;= #{startDate}</if> " +
            "<if test=\"endDate != null\">AND create_time &lt;= #{endDate}</if> " +
            "GROUP BY `date` ORDER BY `date`" +
            "</script>")
    List<Map<String, Object>> countByDate(@Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate,
                                          @Param("granularity") String granularity);

    @Select("SELECT role, COUNT(*) as `count` FROM user WHERE is_deleted = 0 GROUP BY role")
    List<Map<String, Object>> countByRole();

    @Select("SELECT gender, COUNT(*) as `count` FROM user WHERE is_deleted = 0 GROUP BY gender")
    List<Map<String, Object>> countByGender();

    @Select("SELECT COALESCE(up.province, '未知') as label, COUNT(*) as `count` " +
            "FROM user u LEFT JOIN user_profile up ON u.id = up.user_id AND up.is_deleted = 0 " +
            "WHERE u.is_deleted = 0 GROUP BY label ORDER BY `count` DESC LIMIT 20")
    List<Map<String, Object>> countByProvince();
}
