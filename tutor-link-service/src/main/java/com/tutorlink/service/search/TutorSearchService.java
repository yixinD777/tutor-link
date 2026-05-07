package com.tutorlink.service.search;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tutorlink.dao.mapper.TutorProfileMapper;
import com.tutorlink.dao.mapper.TutorSubjectMapper;
import com.tutorlink.dao.mapper.UserMapper;
import com.tutorlink.model.dto.tutor.RegionVO;
import com.tutorlink.model.entity.TutorProfile;
import com.tutorlink.model.entity.TutorSubject;
import com.tutorlink.model.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TutorSearchService {

    private final TutorProfileMapper tutorProfileMapper;
    private final TutorSubjectMapper tutorSubjectMapper;
    private final UserMapper userMapper;

    /**
     * 搜索家教列表 - 支持多条件筛选 + 排序 + 分页
     */
    public IPage<TutorProfile> searchTutors(Long subjectId, String grade,
                                             Integer hourlyRateMin, Integer hourlyRateMax,
                                             String province, String city, String district,
                                             BigDecimal longitude, BigDecimal latitude,
                                             Double distanceKm,
                                             String keyword,
                                             String sortBy, Integer page, Integer size) {

        LambdaQueryWrapper<TutorProfile> wrapper = new LambdaQueryWrapper<>();

        // 只查询已认证的家教
        wrapper.eq(TutorProfile::getCertificationStatus, 2);

        // 关键词模糊搜索 (university/major/intro 三字段 OR)
        if (keyword != null && !keyword.isBlank()) {
            String escaped = keyword.trim()
                    .replace("\\", "\\\\")
                    .replace("%", "\\%")
                    .replace("_", "\\_");
            String pattern = "%" + escaped + "%";
            wrapper.and(w -> w
                    .like(TutorProfile::getUniversity, pattern)
                    .or().like(TutorProfile::getMajor, pattern)
                    .or().like(TutorProfile::getIntro, pattern));
        }

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

        IPage<TutorProfile> result = tutorProfileMapper.selectPage(new Page<>(page, size), wrapper);
        fillUserInfo(result.getRecords());
        return result;
    }

    private void fillUserInfo(List<TutorProfile> profiles) {
        if (profiles == null || profiles.isEmpty()) return;
        List<Long> userIds = profiles.stream().map(TutorProfile::getUserId).toList();
        List<User> users = userMapper.selectBatchIds(userIds);
        Map<Long, User> userMap = users.stream().collect(Collectors.toMap(User::getId, u -> u));
        for (TutorProfile p : profiles) {
            User u = userMap.get(p.getUserId());
            if (u != null) {
                p.setAvatarUrl(u.getAvatarUrl());
                p.setNickname(u.getNickname());
            }
        }
    }

    /**
     * 获取家教详情
     */
    public TutorProfile getTutorDetail(Long userId) {
        TutorProfile profile = tutorProfileMapper.selectOne(
                new LambdaQueryWrapper<TutorProfile>().eq(TutorProfile::getUserId, userId));
        if (profile != null) {
            fillUserInfo(List.of(profile));
        }
        return profile;
    }

    /**
     * 获取有认证家教的地区列表
     */
    public List<RegionVO> getAvailableRegions() {
        List<TutorProfile> profiles = tutorProfileMapper.selectList(
                new LambdaQueryWrapper<TutorProfile>()
                        .eq(TutorProfile::getCertificationStatus, 2)
                        .select(TutorProfile::getProvince, TutorProfile::getCity)
                        .isNotNull(TutorProfile::getProvince)
                        .isNotNull(TutorProfile::getCity)
                        .groupBy(TutorProfile::getProvince, TutorProfile::getCity));
        Map<String, List<String>> grouped = profiles.stream()
                .collect(Collectors.groupingBy(
                        TutorProfile::getProvince,
                        Collectors.mapping(TutorProfile::getCity, Collectors.toList())));
        List<RegionVO> regions = new ArrayList<>();
        grouped.forEach((province, cities) -> {
            List<String> distinctCities = cities.stream().distinct().toList();
            regions.add(new RegionVO(province, distinctCities));
        });
        return regions;
    }

    /**
     * 获取家教的科目列表
     */
    public List<TutorSubject> getTutorSubjects(Long tutorUserId) {
        return tutorSubjectMapper.selectList(
                new LambdaQueryWrapper<TutorSubject>().eq(TutorSubject::getTutorUserId, tutorUserId));
    }
}
