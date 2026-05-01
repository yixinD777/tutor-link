package com.tutorlink.service.search;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tutorlink.dao.mapper.TutorProfileMapper;
import com.tutorlink.dao.mapper.TutorSubjectMapper;
import com.tutorlink.model.entity.TutorProfile;
import com.tutorlink.model.entity.TutorSubject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TutorSearchService {

    private final TutorProfileMapper tutorProfileMapper;
    private final TutorSubjectMapper tutorSubjectMapper;

    /**
     * 搜索家教列表 - 支持多条件筛选 + 排序 + 分页
     */
    public IPage<TutorProfile> searchTutors(Long subjectId, String grade,
                                             Integer hourlyRateMin, Integer hourlyRateMax,
                                             String province, String city, String district,
                                             BigDecimal longitude, BigDecimal latitude,
                                             Double distanceKm,
                                             String sortBy, Integer page, Integer size) {

        LambdaQueryWrapper<TutorProfile> wrapper = new LambdaQueryWrapper<>();

        // 只查询已认证的家教
        wrapper.eq(TutorProfile::getCertificationStatus, 2);

        // 科目筛选 - 先查 tutor_subject 表获取 userId 列表
        if (subjectId != null) {
            List<TutorSubject> tsList = tutorSubjectMapper.selectList(
                    new LambdaQueryWrapper<TutorSubject>().eq(TutorSubject::getSubjectId, subjectId));
            if (tsList.isEmpty()) {
                return new Page<>(page, size);
            }
            List<Long> userIds = tsList.stream().map(TutorSubject::getTutorUserId).toList();
            wrapper.in(TutorProfile::getUserId, userIds);
        }

        // 时薪范围筛选
        if (hourlyRateMin != null) {
            wrapper.ge(TutorProfile::getHourlyRateMin, hourlyRateMin);
        }
        if (hourlyRateMax != null) {
            wrapper.le(TutorProfile::getHourlyRateMax, hourlyRateMax);
        }

        // 地区筛选
        if (province != null) {
            wrapper.eq(TutorProfile::getProvince, province);
        }
        if (city != null) {
            wrapper.eq(TutorProfile::getCity, city);
        }
        if (district != null) {
            wrapper.eq(TutorProfile::getDistrict, district);
        }

        // 经纬度边界框筛选 (简化版附近搜索)
        if (longitude != null && latitude != null && distanceKm != null) {
            // 1度纬度 ≈ 111km, 1度经度 ≈ 111 * cos(纬度) km
            BigDecimal latOffset = BigDecimal.valueOf(distanceKm / 111.0);
            BigDecimal lngOffset = BigDecimal.valueOf(distanceKm / (111.0 * Math.cos(Math.toRadians(latitude.doubleValue()))));

            wrapper.ge(TutorProfile::getLatitude, latitude.subtract(latOffset));
            wrapper.le(TutorProfile::getLatitude, latitude.add(latOffset));
            wrapper.ge(TutorProfile::getLongitude, longitude.subtract(lngOffset));
            wrapper.le(TutorProfile::getLongitude, longitude.add(lngOffset));
        }

        // 排序
        if ("rating".equals(sortBy)) {
            wrapper.orderByDesc(TutorProfile::getAvgRating);
        } else if ("price_asc".equals(sortBy)) {
            wrapper.orderByAsc(TutorProfile::getHourlyRateMin);
        } else if ("price_desc".equals(sortBy)) {
            wrapper.orderByDesc(TutorProfile::getHourlyRateMin);
        } else if ("order_count".equals(sortBy)) {
            wrapper.orderByDesc(TutorProfile::getOrderCount);
        } else {
            // 默认按评分降序
            wrapper.orderByDesc(TutorProfile::getAvgRating);
        }

        return tutorProfileMapper.selectPage(new Page<>(page, size), wrapper);
    }

    /**
     * 获取家教详情
     */
    public TutorProfile getTutorDetail(Long userId) {
        return tutorProfileMapper.selectOne(
                new LambdaQueryWrapper<TutorProfile>().eq(TutorProfile::getUserId, userId));
    }

    /**
     * 获取家教的科目列表
     */
    public List<TutorSubject> getTutorSubjects(Long tutorUserId) {
        return tutorSubjectMapper.selectList(
                new LambdaQueryWrapper<TutorSubject>().eq(TutorSubject::getTutorUserId, tutorUserId));
    }
}
