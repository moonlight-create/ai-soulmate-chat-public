package com.wj.aisoulmatechat.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("soulmate_avatar")
public class SoulmateAvatar {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long soulmateId;
    private String avatarUrl;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
