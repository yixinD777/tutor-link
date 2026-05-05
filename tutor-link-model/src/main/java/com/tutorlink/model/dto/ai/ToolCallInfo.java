package com.tutorlink.model.dto.ai;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class ToolCallInfo {

    private String toolName;
    private Map<String, Object> input;
    private String resultSummary;
}
