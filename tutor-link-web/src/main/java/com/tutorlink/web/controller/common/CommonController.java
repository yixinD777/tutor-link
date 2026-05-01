package com.tutorlink.web.controller.common;

import com.tutorlink.common.response.ApiResult;
import com.tutorlink.dao.mapper.AreaMapper;
import com.tutorlink.dao.mapper.SubjectMapper;
import com.tutorlink.model.entity.Area;
import com.tutorlink.model.entity.Subject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "通用接口")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CommonController {

    private final SubjectMapper subjectMapper;
    private final AreaMapper areaMapper;

    @Operation(summary = "获取科目列表")
    @GetMapping("/subjects")
    public ApiResult<List<Subject>> listSubjects() {
        return ApiResult.success(subjectMapper.selectList(null));
    }

    @Operation(summary = "获取地区列表")
    @GetMapping("/areas")
    public ApiResult<List<Area>> listAreas() {
        return ApiResult.success(areaMapper.selectList(null));
    }
}
