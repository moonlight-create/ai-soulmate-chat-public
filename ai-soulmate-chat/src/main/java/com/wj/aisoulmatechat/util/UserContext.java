package com.wj.aisoulmatechat.util;
import com.wj.aisoulmatechat.entity.AppUserEntity;
import com.wj.aisoulmatechat.security.LoginUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class UserContext {
    public static AppUserEntity getLoginUser(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth==null || auth.getPrincipal() instanceof String){
            return null;
        }
        LoginUser loginUser = (LoginUser) auth.getPrincipal();
        return loginUser.getUser();
    }
}
