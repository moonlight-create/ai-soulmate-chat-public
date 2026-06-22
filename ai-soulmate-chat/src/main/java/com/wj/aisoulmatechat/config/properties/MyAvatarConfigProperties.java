package com.wj.aisoulmatechat.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "my.avatar")
@Data
public class MyAvatarConfigProperties {
    private String defaultUrl = "/avatar/soulmate/default/default_avatar.png";
}
