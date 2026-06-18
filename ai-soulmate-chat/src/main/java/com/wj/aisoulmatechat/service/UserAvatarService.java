package com.wj.aisoulmatechat.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wj.aisoulmatechat.entity.UserAvatarEntity;

public interface UserAvatarService extends IService<UserAvatarEntity> {
    String getUserAvatar(Long userId);
    void saveOrUpdateAvatar(Long userId, String avatarUrl);
}
