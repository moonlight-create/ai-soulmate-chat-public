package com.wj.aisoulmatechat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wj.aisoulmatechat.entity.AppUser;
import com.wj.aisoulmatechat.entity.UserAvatar;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface UserMapper extends BaseMapper<UserAvatar> {
    @Select("select * from app_user where username=#{username}")
    AppUser getByUsername(@Param("username") String username);
}
