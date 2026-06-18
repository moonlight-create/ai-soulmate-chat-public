package com.wj.aisoulmatechat.controller;

import com.wj.aisoulmatechat.common.result.Result;
import com.wj.aisoulmatechat.service.SoulmateAvatarService;
import com.wj.aisoulmatechat.service.impl.SoulmateAvatarServiceImpl;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/avatar/soulmate")
@Validated
public class SoulmateAvatarController {
    private final SoulmateAvatarService soulmateAvatarService;

    // 根据伴侣ID查询头像链接，无数据返回默认头像
    @GetMapping("/get")
    public Result<String> getAvatar(
            @NotNull(message = "伴侣ID不能为空") @RequestParam Long soulmateId
    ) {
        String url = soulmateAvatarService.getSoumateAvatar(soulmateId);
        String res = url == null ? SoulmateAvatarServiceImpl.DEFAULT_AVATAR : url;
        return Result.ok(res);
    }

    /**
     * 上传图片 + 更新该伴侣头像
     */
    @PostMapping("/uploadAndSave")
    public Result<String> uploadAndSave(
            @NotNull(message = "上传文件不能为空") @RequestParam("file") MultipartFile file,
            @NotNull(message = "伴侣ID不能为空") @RequestParam Long soulmateId
    ) throws Exception {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件内容不能为空");
        }
        String avatarUrl = soulmateAvatarService.uploadFile(file);
        soulmateAvatarService.saveOrUpdateAvatar(soulmateId, avatarUrl);
        return Result.ok(avatarUrl);
    }

    /**
     * 上传图片
     */
    @PostMapping("/upload")
    public Result<String> upload(
            @NotNull(message = "上传文件不能为空") @RequestParam("file") MultipartFile file
    ) throws Exception {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件内容不能为空");
        }
        String res = soulmateAvatarService.uploadFile(file);
        return Result.ok(res);
    }
}
