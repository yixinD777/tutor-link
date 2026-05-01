package com.tutorlink.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.tutorlink.model.entity.MpBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("student_certification")
public class StudentCertification extends MpBaseEntity {

    private Long userId;

    private String realName;

    private String idCardNo;

    private String studentIdNo;

    private String studentIdPhotoFront;

    private String studentIdPhotoBack;

    private String handheldPhoto;

    private String ocrResult;

    private Integer status;

    private String rejectReason;

    private Long reviewerId;

    private LocalDateTime reviewTime;
}
