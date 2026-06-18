package com.wj.aisoulmatechat.util;

import com.wj.aisoulmatechat.security.LoginUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 登录用户信息工具类
 */
public class SecurityUserUtil {

    /**
     * 获取当前登录用户
     * @return LoginUser
     */
    public static LoginUser getCurrentLoginUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() instanceof String) {
            throw new SecurityException("用户未登录，请重新登录");
        }
        return (LoginUser) authentication.getPrincipal();
    }

    /**
     * 获取当前登录用户ID
     */
    public static Long getCurrentUserId() {
        return getCurrentLoginUser().getUser().getId();
    }
}
