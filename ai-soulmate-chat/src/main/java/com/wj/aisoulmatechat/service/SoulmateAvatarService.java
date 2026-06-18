package com.wj.aisoulmatechat.service;

import com.wj.aisoulmatechat.vo.SoulmateAvatarVO;
import org.springframework.web.multipart.MultipartFile;

public interface SoulmateAvatarService {

    /**
     * 根据伴侣ID获取头像地址
     */
    String getSoumateAvatar(Long soulmateId);

    /**
     * 新增/更新伴侣头像数据库记录
     */
    void saveOrUpdateAvatar(Long soulmateId, String avatarUrl);

    /**
     * 根据伴侣ID查询头像
     */
    SoulmateAvatarVO getBySoulmateId(Long sid);

    /**
     * 上传文件到本地
     */
    String uploadFile(MultipartFile file) throws Exception;
}
