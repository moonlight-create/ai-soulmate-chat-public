package com.wj.aisoulmatechat.security;
import com.wj.aisoulmatechat.entity.AppUserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.Collections;

@RequiredArgsConstructor
public class LoginUser implements UserDetails {
    private static final long serialVersionUID = 1L;

    private final AppUserEntity user;

    public AppUserEntity getUser(){
        return user;
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }
    @Override
    public String getPassword() {
        return user.getPassword();
    }
    @Override
    public boolean isEnabled() {
        return user.getEnable() ==1;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }
    @Override public boolean isAccountNonExpired() {return true;}
    @Override public boolean isAccountNonLocked() {return true;}
    @Override public boolean isCredentialsNonExpired() {return true;}
}
