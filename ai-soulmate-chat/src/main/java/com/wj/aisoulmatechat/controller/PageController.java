package com.wj.aisoulmatechat.controller;

import com.wj.aisoulmatechat.service.SoulmateAvatarService;
import com.wj.aisoulmatechat.service.SoulmateService;
import com.wj.aisoulmatechat.util.AgeCulUtil;
import com.wj.aisoulmatechat.util.SecurityUserUtil;
import com.wj.aisoulmatechat.vo.SoulmateAvatarVO;
import com.wj.aisoulmatechat.vo.SoulmateVO;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class PageController {
    private final SoulmateService soulmateService;
    private final SoulmateAvatarService soulmateAvatarService;
    private final RedisProperties redisProperties;

    //跳转登录页
    @GetMapping({"/toLogin","/login"})
    public String toLogin(){
        return "login";
    }

//    //跳转登录页
//    @GetMapping("/login")
//    public String login(){
//        return "login";
//    }

    // 跳转聊天房间页面，携带soulmateId
    @GetMapping("/toChat")
    public String toChat(@RequestParam("sid") Long sid, Model model){
        SoulmateVO soulmateVo = soulmateService.getById(sid);

        String birth = soulmateVo.getBirth();
        birth = Optional.ofNullable(birth).orElse("未设置");

        int age = 18;
        if(!"未设置".equals(birth)){
            age = AgeCulUtil.getAge(birth);
        }
        soulmateVo.setAge(age);

        SoulmateAvatarVO soulmateAvatarVo =  soulmateAvatarService.getBySoulmateId(sid);

        soulmateAvatarVo = Optional.ofNullable(soulmateAvatarVo)
                .orElse(new SoulmateAvatarVO());

        model.addAttribute("soulmate_info",soulmateVo);
        model.addAttribute("soulmate_avatar_info",soulmateAvatarVo);

        return "chat";
    }

    // 跳转伴侣选择首页
    @GetMapping({"/","select_soulmate"})
    public String index(Model model){
        Long userId = SecurityUserUtil.getCurrentUserId();
        List<SoulmateVO> list = soulmateService.getList(userId);
        model.addAttribute("list",list);
        return "select_soulmate";
    }




}
