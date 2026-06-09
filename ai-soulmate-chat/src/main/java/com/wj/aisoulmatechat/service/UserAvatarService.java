package com.wj.aisoulmatechat.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wj.aisoulmatechat.entity.UserAvatar;

public interface UserAvatarService extends IService<UserAvatar> {
    String getUserAvatar(Long userId);
    void saveOrUpdateAvatar(Long userId, String avatarUrl);
}
