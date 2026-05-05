package com.tutorlink.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tutorlink.model.entity.AiUserMemory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AiUserMemoryMapper extends BaseMapper<AiUserMemory> {

    @Select("SELECT * FROM ai_user_memory WHERE user_id = #{userId} AND is_deleted = 0 LIMIT 1")
    AiUserMemory selectByUserId(@Param("userId") Long userId);
}
