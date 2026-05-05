package com.tutorlink.model.dto.ai;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AiChatResponse {

    private String message;
    private String conversationId;
    private List<ToolCallInfo> toolCalls;
}
