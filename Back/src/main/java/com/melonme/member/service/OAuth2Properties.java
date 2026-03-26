package com.melonme.member.service;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "oauth2")
public class OAuth2Properties {

    private ProviderConfig kakao = new ProviderConfig();
    private ProviderConfig google = new ProviderConfig();

    @Getter
    @Setter
    public static class ProviderConfig {
        private String clientId;
        private String clientSecret;
        private String redirectUri;
        private String tokenUri;
        private String userInfoUri;
    }
}
