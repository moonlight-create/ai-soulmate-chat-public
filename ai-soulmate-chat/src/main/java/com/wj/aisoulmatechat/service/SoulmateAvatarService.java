package com.wj.aisoulmatechat.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wj.aisoulmatechat.entity.SoulmateAvatarEntity;
import com.wj.aisoulmatechat.vo.SoulmateAvatarVo;

public interface SoulmateAvatarService extends IService<SoulmateAvatarEntity> {
    String getSoumateAvatar(Long soulmateId);
    void saveOrUpdateAvatar(Long soulmateId, String avatarUrl);
    SoulmateAvatarVo getBySoulmateId(Long sid);
}
