package com.tutorlink.service.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tutorlink.common.constant.CertificationStatus;
import com.tutorlink.common.constant.ResultCode;
import com.tutorlink.common.exception.BusinessException;
import com.tutorlink.dao.mapper.StudentCertificationMapper;
import com.tutorlink.dao.mapper.TutorProfileMapper;
import com.tutorlink.model.entity.StudentCertification;
import com.tutorlink.model.entity.TutorProfile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class CertificationService {

    private final StudentCertificationMapper certificationMapper;
    private final TutorProfileMapper tutorProfileMapper;

    /**
     * 提交学生认证
     */
    @Transactional(rollbackFor = Exception.class)
    public StudentCertification applyCertification(Long userId, String realName,
                                                    String idCardNo, String studentIdNo,
                                                    String photoFront, String photoBack,
                                                    String handheldPhoto, String ocrResult) {
        // 检查是否已有待审核的认证
        Long pendingCount = certificationMapper.selectCount(
                new LambdaQueryWrapper<StudentCertification>()
                        .eq(StudentCertification::getUserId, userId)
                        .eq(StudentCertification::getStatus, CertificationStatus.PENDING.getCode()));
        if (pendingCount > 0) {
            throw new BusinessException(ResultCode.CERTIFICATION_PENDING);
        }

        StudentCertification cert = new StudentCertification();
        cert.setUserId(userId);
        cert.setRealName(realName);
        cert.setIdCardNo(idCardNo);
        cert.setStudentIdNo(studentIdNo);
        cert.setStudentIdPhotoFront(photoFront);
        cert.setStudentIdPhotoBack(photoBack);
        cert.setHandheldPhoto(handheldPhoto);
        cert.setOcrResult(ocrResult);
        cert.setStatus(CertificationStatus.PENDING.getCode());
        certificationMapper.insert(cert);

        // 更新家教档案认证状态为审核中
        TutorProfile profile = tutorProfileMapper.selectOne(
                new LambdaQueryWrapper<TutorProfile>().eq(TutorProfile::getUserId, userId));
        if (profile != null) {
            profile.setCertificationStatus(CertificationStatus.PENDING.getCode());
            tutorProfileMapper.updateById(profile);
        }

        return cert;
    }

    /**
     * 获取当前认证状态
     */
    public StudentCertification getMyCertification(Long userId) {
        return certificationMapper.selectOne(
                new LambdaQueryWrapper<StudentCertification>()
                        .eq(StudentCertification::getUserId, userId)
                        .orderByDesc(StudentCertification::getCreateTime)
                        .last("LIMIT 1"));
    }

    /**
     * 管理员 - 获取待审核认证列表
     */
    public IPage<StudentCertification> listPendingCertifications(int page, int size) {
        return certificationMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<StudentCertification>()
                        .eq(StudentCertification::getStatus, CertificationStatus.PENDING.getCode())
                        .orderByAsc(StudentCertification::getCreateTime));
    }

    /**
     * 管理员 - 审核通过
     */
    @Transactional(rollbackFor = Exception.class)
    public void approveCertification(Long certId, Long reviewerId) {
        StudentCertification cert = certificationMapper.selectById(certId);
        if (cert == null) {
            throw new BusinessException(ResultCode.CERTIFICATION_NOT_FOUND);
        }
        if (cert.getStatus() != CertificationStatus.PENDING.getCode()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该认证不在审核状态");
        }

        cert.setStatus(CertificationStatus.APPROVED.getCode());
        cert.setReviewerId(reviewerId);
        cert.setReviewTime(LocalDateTime.now());
        certificationMapper.updateById(cert);

        // 更新家教档案认证状态
        TutorProfile profile = tutorProfileMapper.selectOne(
                new LambdaQueryWrapper<TutorProfile>().eq(TutorProfile::getUserId, cert.getUserId()));
        if (profile != null) {
            profile.setCertificationStatus(CertificationStatus.APPROVED.getCode());
            profile.setCertificationTime(LocalDateTime.now());
            tutorProfileMapper.updateById(profile);
        }
    }

    /**
     * 管理员 - 审核拒绝
     */
    @Transactional(rollbackFor = Exception.class)
    public void rejectCertification(Long certId, Long reviewerId, String reason) {
        StudentCertification cert = certificationMapper.selectById(certId);
        if (cert == null) {
            throw new BusinessException(ResultCode.CERTIFICATION_NOT_FOUND);
        }
        if (cert.getStatus() != CertificationStatus.PENDING.getCode()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该认证不在审核状态");
        }

        cert.setStatus(CertificationStatus.REJECTED.getCode());
        cert.setRejectReason(reason);
        cert.setReviewerId(reviewerId);
        cert.setReviewTime(LocalDateTime.now());
        certificationMapper.updateById(cert);

        // 更新家教档案认证状态
        TutorProfile profile = tutorProfileMapper.selectOne(
                new LambdaQueryWrapper<TutorProfile>().eq(TutorProfile::getUserId, cert.getUserId()));
        if (profile != null) {
            profile.setCertificationStatus(CertificationStatus.REJECTED.getCode());
            tutorProfileMapper.updateById(profile);
        }
    }
}
