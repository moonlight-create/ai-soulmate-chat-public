package com.wj.aisoulmatechat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wj.aisoulmatechat.entity.UserAvatarEntity;
import com.wj.aisoulmatechat.mapper.UserAvatarMapper;
import com.wj.aisoulmatechat.service.UserAvatarService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserAvatarServiceImpl extends ServiceImpl<UserAvatarMapper, UserAvatarEntity>
        implements UserAvatarService {

    @Override
    public String getUserAvatar(Long userId) {
        UserAvatarEntity one = getOne(new LambdaQueryWrapper<UserAvatarEntity>()
                .eq(UserAvatarEntity::getUserId, userId)
                .last("limit 1"));
        return one == null ? null : one.getAvatarUrl();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdateAvatar(Long userId, String avatarUrl) {
        UserAvatarEntity one = getOne(new LambdaQueryWrapper<UserAvatarEntity>()
                .eq(UserAvatarEntity::getUserId, userId)
                .last("limit 1"));

        if (one == null) {
            one = new UserAvatarEntity();
            one.setUserId(userId);
            one.setAvatarUrl(avatarUrl);
            save(one);
        } else {
            one.setAvatarUrl(avatarUrl);
            updateById(one);
        }
    }
}
