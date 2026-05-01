package com.tutorlink.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tutorlink.model.entity.ChatConversation;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatConversationMapper extends BaseMapper<ChatConversation> {
}
