package com.wj.aisoulmatechat.controller;

import com.wj.aisoulmatechat.common.result.Result;
import com.wj.aisoulmatechat.dto.UserSoulmateDTO;
import com.wj.aisoulmatechat.entity.SoulmateAvatarEntity;
import com.wj.aisoulmatechat.service.SoulmateService;
import com.wj.aisoulmatechat.util.SecurityUserUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import opennlp.tools.util.StringUtil;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/setting/soulmate")
@Validated
public class SoulmateSettingController {
    private final SoulmateService soulmateService;
    //默认头像
    private static final String DEFAULT_AVATAR = "https://picsum.photos/id/64/300/300";

    // 新增伴侣提交
    @PostMapping("/add")
    public Result<String> add(
        @Valid UserSoulmateDTO soul,
        @RequestParam("avatarUrl") String avatarUrl
    ){
        Long userId = SecurityUserUtil.getCurrentUserId();
        soul.setUserId(userId);
        //填充默认字段
        soul.setDefaultValue();
        soulmateService.saveSoulmate(soul, StringUtil.isEmpty(avatarUrl) ? DEFAULT_AVATAR : avatarUrl);
        return Result.ok("创建成功","/select_soulmate");
    }

    // 修改伴侣提交
    @PostMapping("/update")
    public Result<String> update(UserSoulmateDTO soul,
                                 @NotNull(message = "头像ID不能为空") @RequestParam("avatarId") Long avatarId,
                                 @NotNull(message = "头像链接不能为空") @RequestParam("avatarUrl") String avatarUrl
    ){
        Long userId = SecurityUserUtil.getCurrentUserId();
        soul.setUserId(userId);
        SoulmateAvatarEntity soulmateAvatarEntity = new SoulmateAvatarEntity();
        soulmateAvatarEntity.setId(avatarId);
        soulmateAvatarEntity.setAvatarUrl(avatarUrl);
        soulmateService.updateById(soul, soulmateAvatarEntity);
        return Result.ok("修改成功","/toChat?sid=" + soul.getId());
    }

    // 根据id删除伴侣 + 头像
    @PostMapping("/del/{soulmateId}")
    public Result<Boolean> delete(
        @NotNull(message = "伴侣ID不能为空") @PathVariable("soulmateId") Long soulmateId
    ){
        // 增加鉴权，防止越权删除
        boolean success = soulmateService.deleteById(soulmateId);
        if(success){
            return Result.ok("删除成功",true);
        }else{
            return Result.fail("删除失败，数据不存在或无权限");
        }
    }
}
