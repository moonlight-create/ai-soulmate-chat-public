package com.wj.aisoulmatechat.controller;

import com.wj.aisoulmatechat.entity.SoulmateAvatar;
import com.wj.aisoulmatechat.entity.UserSoulmate;
import com.wj.aisoulmatechat.service.SoulmateService;
import lombok.RequiredArgsConstructor;
import opennlp.tools.util.StringUtil;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.wj.aisoulmatechat.security.LoginUser;

import java.util.Optional;

@Controller()
@RequiredArgsConstructor
@RequestMapping("/setting/soulmate")
public class SoulmateSettingController {
    private final SoulmateService soulmateService;
    //默认头像
    private static final String DEFAULT_AVATAR = "https://picsum.photos/id/64/300/300";

    // 新增伴侣提交
    @PostMapping("/add")
    public String add(UserSoulmate soul, @RequestParam("avatarUrl")String avatarUrl, Authentication auth){
        LoginUser loginUser = (LoginUser) auth.getPrincipal();
        Long userId = loginUser.getUser().getId();
        soul.setUserId(userId);
        soulmateService.saveSoulmate(soul,StringUtil.isEmpty(avatarUrl)?DEFAULT_AVATAR:avatarUrl);
        return "redirect:/select_soulmate";
    }

    // 修改伴侣提交
    @PostMapping("/update")
    public String update(UserSoulmate soul,
                         @RequestParam("avatarId") Long avatarId,
                         @RequestParam("avatarUrl") String avatarUrl,Authentication auth){
        LoginUser loginUser = (LoginUser) auth.getPrincipal();
        Long userId = loginUser.getUser().getId();
        soul.setUserId(userId);
        SoulmateAvatar soulmateAvatar = new SoulmateAvatar();
        soulmateAvatar.setId(avatarId);
        soulmateAvatar.setAvatarUrl(avatarUrl);
        soulmateService.updateById(soul, soulmateAvatar);
        return "redirect:/toChat?sid="+soul.getId();
    }


    // 根据id删除伴侣 + 头像
    @PostMapping("/del/{id}")
    public String delete(@PathVariable("id") Long id){
        soulmateService.deleteById(id);
        return "redirect:/select_soulmate";
    }
}
