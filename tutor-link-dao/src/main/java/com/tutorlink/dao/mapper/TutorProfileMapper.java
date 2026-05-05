package com.tutorlink.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tutorlink.model.entity.TutorProfile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface TutorProfileMapper extends BaseMapper<TutorProfile> {

    @Select("SELECT " +
            "CASE " +
            "WHEN avg_rating IS NULL OR avg_rating = 0 THEN '未评分' " +
            "WHEN avg_rating &lt; 2 THEN '1-2分' " +
            "WHEN avg_rating &lt; 3 THEN '2-3分' " +
            "WHEN avg_rating &lt; 4 THEN '3-4分' " +
            "ELSE '4-5分' " +
            "END as label, COUNT(*) as `count` " +
            "FROM tutor_profile WHERE is_deleted = 0 " +
            "GROUP BY label ORDER BY label")
    List<Map<String, Object>> countByRatingRange();

    @Select("SELECT COALESCE(university, '未知') as label, COUNT(*) as `count` " +
            "FROM tutor_profile WHERE is_deleted = 0 " +
            "GROUP BY label ORDER BY `count` DESC LIMIT #{limit}")
    List<Map<String, Object>> countByUniversity(@Param("limit") int limit);

    @Select("SELECT certification_status, COUNT(*) as `count` " +
            "FROM tutor_profile WHERE is_deleted = 0 GROUP BY certification_status")
    List<Map<String, Object>> countByCertificationStatus();

    @Select("SELECT " +
            "CASE " +
            "WHEN hourly_rate_min IS NULL THEN '未设置' " +
            "WHEN hourly_rate_min &lt; 50 THEN '50以下' " +
            "WHEN hourly_rate_min &lt; 100 THEN '50-100' " +
            "WHEN hourly_rate_min &lt; 150 THEN '100-150' " +
            "WHEN hourly_rate_min &lt; 200 THEN '150-200' " +
            "ELSE '200以上' " +
            "END as label, COUNT(*) as `count` " +
            "FROM tutor_profile WHERE is_deleted = 0 " +
            "GROUP BY label ORDER BY label")
    List<Map<String, Object>> countByHourlyRateRange();
}
