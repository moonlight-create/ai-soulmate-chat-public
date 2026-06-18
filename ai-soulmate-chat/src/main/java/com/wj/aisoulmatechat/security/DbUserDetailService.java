package com.wj.aisoulmatechat.security;
import com.wj.aisoulmatechat.entity.AppUserEntity;
import com.wj.aisoulmatechat.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DbUserDetailService implements UserDetailsService {
    private final UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUserEntity user = userMapper.getByUsername(username);
        if(user==null){
            throw new UsernameNotFoundException("用户不存在");
        }
        return new LoginUser(user);
    }
}
