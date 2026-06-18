package com.wj.aisoulmatechat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wj.aisoulmatechat.entity.AppUserEntity;
import com.wj.aisoulmatechat.entity.UserAvatarEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface UserMapper extends BaseMapper<UserAvatarEntity> {
    @Select("select * from app_user where username=#{username}")
    AppUserEntity getByUsername(@Param("username") String username);
}
