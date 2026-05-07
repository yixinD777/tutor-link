package com.tutorlink.web.controller.tutor;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tutorlink.common.response.ApiResult;
import com.tutorlink.model.dto.tutor.RegionVO;
import com.tutorlink.model.entity.TutorProfile;
import com.tutorlink.model.entity.TutorSubject;
import com.tutorlink.service.search.TutorSearchService;
import com.tutorlink.service.user.TutorProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Tag(name = "家教管理")
@RestController
@RequestMapping("/api/v1/tutors")
@RequiredArgsConstructor
public class TutorController {

    private final TutorSearchService tutorSearchService;
    private final TutorProfileService tutorProfileService;

    @Operation(summary = "搜索家教列表")
    @GetMapping
    public ApiResult<IPage<TutorProfile>> searchTutors(
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) String grade,
            @RequestParam(required = false) Integer hourlyRateMin,
            @RequestParam(required = false) Integer hourlyRateMax,
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) BigDecimal longitude,
            @RequestParam(required = false) BigDecimal latitude,
            @RequestParam(required = false) Double distanceKm,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "rating") String sortBy,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        return ApiResult.success(tutorSearchService.searchTutors(
                subjectId, grade, hourlyRateMin, hourlyRateMax,
                province, city, district, longitude, latitude, distanceKm,
                keyword, sortBy, page, size));
    }

    @Operation(summary = "获取有家教的地区列表")
    @GetMapping("/regions")
    public ApiResult<List<RegionVO>> getAvailableRegions() {
        return ApiResult.success(tutorSearchService.getAvailableRegions());
    }

    @Operation(summary = "获取家教详情")
    @GetMapping("/{userId}")
    public ApiResult<TutorProfile> getTutorDetail(@PathVariable Long userId) {
        return ApiResult.success(tutorSearchService.getTutorDetail(userId));
    }

    @Operation(summary = "获取家教科目列表")
    @GetMapping("/{userId}/subjects")
    public ApiResult<List<TutorSubject>> getTutorSubjects(@PathVariable Long userId) {
        return ApiResult.success(tutorSearchService.getTutorSubjects(userId));
    }

    @Operation(summary = "初始化家教档案")
    @PreAuthorize("hasRole('PARENT')")
    @PostMapping("/me/init")
    public ApiResult<TutorProfile> initTutorProfile(@AuthenticationPrincipal Long userId) {
        return ApiResult.success(tutorProfileService.initTutorProfile(userId));
    }

    @Operation(summary = "更新家教档案")
    @PreAuthorize("hasRole('TUTOR')")
    @PutMapping("/me/profile")
    public ApiResult<TutorProfile> updateTutorProfile(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) String university,
            @RequestParam(required = false) String major,
            @RequestParam(required = false) Integer enrollmentYear,
            @RequestParam(required = false) Integer educationLevel,
            @RequestParam(required = false) String intro,
            @RequestParam(required = false) String teachingStyle,
            @RequestParam(required = false) Integer hourlyRateMin,
            @RequestParam(required = false) Integer hourlyRateMax,
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) BigDecimal longitude,
            @RequestParam(required = false) BigDecimal latitude) {
        return ApiResult.success(tutorProfileService.updateTutorProfile(
                userId, university, major, enrollmentYear, educationLevel,
                intro, teachingStyle, hourlyRateMin, hourlyRateMax,
                province, city, district, longitude, latitude));
    }

    @Operation(summary = "添加家教科目")
    @PreAuthorize("hasRole('TUTOR')")
    @PostMapping("/me/subjects")
    public ApiResult<Void> addSubject(
            @AuthenticationPrincipal Long userId,
            @RequestParam Long subjectId,
            @RequestParam(required = false) String gradeRange) {
        tutorProfileService.addSubject(userId, subjectId, gradeRange);
        return ApiResult.success();
    }

    @Operation(summary = "删除家教科目")
    @PreAuthorize("hasRole('TUTOR')")
    @DeleteMapping("/me/subjects/{subjectId}")
    public ApiResult<Void> removeSubject(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long subjectId) {
        tutorProfileService.removeSubject(userId, subjectId);
        return ApiResult.success();
    }
}
