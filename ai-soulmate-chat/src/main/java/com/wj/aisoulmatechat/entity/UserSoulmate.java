package com.wj.aisoulmatechat.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_soulmate")
public class UserSoulmate {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String gfName;
    private String characterTag;
    private String detailPrompt;
    private String sex;
    private Integer age;
    private String birth;
    private String hobby;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
