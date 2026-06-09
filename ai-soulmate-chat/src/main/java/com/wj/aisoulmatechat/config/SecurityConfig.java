package com.wj.aisoulmatechat.config;

import com.wj.aisoulmatechat.security.DbUserDetailService;
import com.wj.aisoulmatechat.security.RedisRememberTokenRepository;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.rememberme.CookieTheftException;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final DbUserDetailService dbUserDetailService;
    private final int REMEMBER_EXPIRE_SEC = 60*60*48;

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public PersistentTokenRepository redisTokenRepo(RedisConnectionFactory factory){
        RedisRememberTokenRepository repo = new RedisRememberTokenRepository(factory, REMEMBER_EXPIRE_SEC);
        return repo;
    }

//    @Bean
//    public PersistentTokenRepository tokenRepository(DataSource ds){
//        JdbcTokenRepositoryImpl repo=new JdbcTokenRepositoryImpl();
//        repo.setDataSource(ds);
//        repo.setCreateTableOnStartup(false);
//        return repo;
//    }

    @Bean
    public DaoAuthenticationProvider authProvider(){
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(dbUserDetailService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,PersistentTokenRepository persistentTokenRepository) throws Exception{
//        http.exceptionHandling(c -> c.accessDeniedHandler(customAccessDeniedHandler));
        http
                .authorizeHttpRequests(auth->auth
                        .requestMatchers("/css/**","/js/**","/h2-console/**","/avatar/soulmate/file/**","/chat/ai-chat-stream").permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request,response,authException)->{
                            // 记住我token被盗/失效异常 → 删cookie跳登录
                            if(authException instanceof CookieTheftException){
                                Cookie cookie = new Cookie("remember-me",null);
                                cookie.setMaxAge(0);
                                cookie.setPath("/");
                                response.addCookie(cookie);
                            }
                            response.sendRedirect("/login");
                        })
                )
                .formLogin(form->form
                        .loginPage("/toLogin")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/select_soulmate",true)
                        .permitAll()
                )
                .logout(log -> log
                        .logoutUrl("/logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("SESSION","remember-me")
                )
                .rememberMe(rm->rm
                          .rememberMeParameter("remember-me")
//                        .tokenRepository(redisTokenRepo(null))
                          .tokenRepository(persistentTokenRepository)
                          .tokenValiditySeconds(REMEMBER_EXPIRE_SEC)

                )
                .headers(header -> header.frameOptions(frame -> frame.disable()))
                .csrf(csrf -> csrf.disable());
        return http.build();
    }

}
