package com.tutorlink.service.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tutorlink.common.constant.CertificationStatus;
import com.tutorlink.common.constant.ResultCode;
import com.tutorlink.common.constant.UserRole;
import com.tutorlink.common.exception.BusinessException;
import com.tutorlink.dao.mapper.TutorProfileMapper;
import com.tutorlink.dao.mapper.TutorSubjectMapper;
import com.tutorlink.dao.mapper.UserMapper;
import com.tutorlink.model.entity.TutorProfile;
import com.tutorlink.model.entity.TutorSubject;
import com.tutorlink.model.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TutorProfileService {

    private final TutorProfileMapper tutorProfileMapper;
    private final TutorSubjectMapper tutorSubjectMapper;
    private final UserMapper userMapper;

    /**
     * 初始化家教档案 (用户切换为家教角色时调用)
     */
    @Transactional(rollbackFor = Exception.class)
    public TutorProfile initTutorProfile(Long userId) {
        TutorProfile existing = tutorProfileMapper.selectOne(
                new LambdaQueryWrapper<TutorProfile>().eq(TutorProfile::getUserId, userId));
        if (existing != null) {
            return existing;
        }

        // 更新用户角色: 替换为 TUTOR 角色（角色互斥，不能同时是家长和家教）
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        if (UserRole.hasRole(user.getRole(), UserRole.TUTOR)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "您已经是家教角色");
        }
        if (!UserRole.hasRole(user.getRole(), UserRole.PARENT)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "仅家长角色可切换为家教");
        }
        user.setRole(UserRole.TUTOR.getCode());
        userMapper.updateById(user);

        // 创建家教档案
        TutorProfile profile = new TutorProfile();
        profile.setUserId(userId);
        profile.setCertificationStatus(CertificationStatus.NONE.getCode());
        profile.setAvgRating(BigDecimal.ZERO);
        profile.setRatingCount(0);
        profile.setOrderCount(0);
        profile.setIsOnline(0);
        tutorProfileMapper.insert(profile);

        return profile;
    }

    /**
     * 更新家教档案
     */
    public TutorProfile updateTutorProfile(Long userId, String university, String major,
                                            Integer enrollmentYear, Integer educationLevel,
                                            String intro, String teachingStyle,
                                            Integer hourlyRateMin, Integer hourlyRateMax,
                                            String province, String city, String district,
                                            BigDecimal longitude, BigDecimal latitude) {
        TutorProfile profile = tutorProfileMapper.selectOne(
                new LambdaQueryWrapper<TutorProfile>().eq(TutorProfile::getUserId, userId));
        if (profile == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND, "家教档案不存在，请先初始化");
        }

        if (university != null) profile.setUniversity(university);
        if (major != null) profile.setMajor(major);
        if (enrollmentYear != null) profile.setEnrollmentYear(enrollmentYear);
        if (educationLevel != null) profile.setEducationLevel(educationLevel);
        if (intro != null) profile.setIntro(intro);
        if (teachingStyle != null) profile.setTeachingStyle(teachingStyle);
        if (hourlyRateMin != null) profile.setHourlyRateMin(hourlyRateMin);
        if (hourlyRateMax != null) profile.setHourlyRateMax(hourlyRateMax);
        if (province != null) profile.setProvince(province);
        if (city != null) profile.setCity(city);
        if (district != null) profile.setDistrict(district);
        if (longitude != null) profile.setLongitude(longitude);
        if (latitude != null) profile.setLatitude(latitude);

        tutorProfileMapper.updateById(profile);
        return profile;
    }

    /**
     * 添加家教科目
     */
    public void addSubject(Long tutorUserId, Long subjectId, String gradeRange) {
        // 检查是否已存在
        Long count = tutorSubjectMapper.selectCount(
                new LambdaQueryWrapper<TutorSubject>()
                        .eq(TutorSubject::getTutorUserId, tutorUserId)
                        .eq(TutorSubject::getSubjectId, subjectId));
        if (count > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "已添加该科目");
        }

        TutorSubject ts = new TutorSubject();
        ts.setTutorUserId(tutorUserId);
        ts.setSubjectId(subjectId);
        ts.setGradeRange(gradeRange);
        tutorSubjectMapper.insert(ts);
    }

    /**
     * 删除家教科目
     */
    public void removeSubject(Long tutorUserId, Long subjectId) {
        tutorSubjectMapper.delete(
                new LambdaQueryWrapper<TutorSubject>()
                        .eq(TutorSubject::getTutorUserId, tutorUserId)
                        .eq(TutorSubject::getSubjectId, subjectId));
    }
}
