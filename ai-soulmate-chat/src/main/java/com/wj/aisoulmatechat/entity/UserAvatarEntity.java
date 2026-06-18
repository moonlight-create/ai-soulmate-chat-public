package com.wj.aisoulmatechat.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user_avatar")
public class UserAvatarEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String avatarUrl;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
