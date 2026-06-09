package com.wj.aisoulmatechat.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wj.aisoulmatechat.entity.SoulmateAvatar;
import com.wj.aisoulmatechat.vo.SoulmateAvatarVo;

public interface SoulmateAvatarService extends IService<SoulmateAvatar> {
    String getSoumateAvatar(Long soulmateId);
    void saveOrUpdateAvatar(Long soulmateId, String avatarUrl);
    SoulmateAvatarVo getBySoulmateId(Long sid);
}
