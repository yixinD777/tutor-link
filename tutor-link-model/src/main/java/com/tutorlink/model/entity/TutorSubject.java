package com.tutorlink.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.tutorlink.model.entity.MpBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tutor_subject")
public class TutorSubject extends MpBaseEntity {

    private Long tutorUserId;

    private Long subjectId;

    private String gradeRange;
}
