package com.melonme.member.service;

import com.melonme.global.exception.CustomException;
import com.melonme.global.exception.ErrorCode;
import com.melonme.member.domain.Provider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2Service {

    private final OAuth2Properties oAuth2Properties;
    private final WebClient webClient = WebClient.builder().build();

    /**
     * OAuth2 인가 코드로 소셜 사용자 정보를 가져온다.
     * @return [providerId, nickname(or email)] 형태의 배열
     */
    public String[] getUserInfo(Provider provider, String code) {
        OAuth2Properties.ProviderConfig config = getConfig(provider);
        String accessToken = getAccessToken(config, code);
        return fetchUserInfo(provider, config, accessToken);
    }

    private OAuth2Properties.ProviderConfig getConfig(Provider provider) {
        return switch (provider) {
            case KAKAO -> oAuth2Properties.getKakao();
            case GOOGLE -> oAuth2Properties.getGoogle();
        };
    }

    @SuppressWarnings("unchecked")
    private String getAccessToken(OAuth2Properties.ProviderConfig config, String code) {
        try {
            Map<String, Object> response = webClient.post()
                    .uri(config.getTokenUri())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData("grant_type", "authorization_code")
                            .with("client_id", config.getClientId())
                            .with("client_secret", config.getClientSecret())
                            .with("redirect_uri", config.getRedirectUri())
                            .with("code", code))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null || !response.containsKey("access_token")) {
                throw new CustomException(ErrorCode.AUTH_OAUTH_FAILED);
            }
            return (String) response.get("access_token");
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("OAuth2 token exchange failed", e);
            throw new CustomException(ErrorCode.AUTH_OAUTH_FAILED);
        }
    }

    @SuppressWarnings("unchecked")
    private String[] fetchUserInfo(Provider provider, OAuth2Properties.ProviderConfig config, String accessToken) {
        try {
            Map<String, Object> response = webClient.get()
                    .uri(config.getUserInfoUri())
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null) {
                throw new CustomException(ErrorCode.AUTH_OAUTH_FAILED);
            }

            return switch (provider) {
                case KAKAO -> parseKakaoUserInfo(response);
                case GOOGLE -> parseGoogleUserInfo(response);
            };
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("OAuth2 user info fetch failed", e);
            throw new CustomException(ErrorCode.AUTH_OAUTH_FAILED);
        }
    }

    @SuppressWarnings("unchecked")
    private String[] parseKakaoUserInfo(Map<String, Object> response) {
        String providerId = String.valueOf(response.get("id"));
        String nickname = "user_" + providerId;

        Map<String, Object> properties = (Map<String, Object>) response.get("properties");
        if (properties != null && properties.get("nickname") != null) {
            nickname = (String) properties.get("nickname");
        }

        return new String[]{providerId, nickname};
    }

    private String[] parseGoogleUserInfo(Map<String, Object> response) {
        String providerId = (String) response.get("id");
        String nickname = (String) response.get("name");
        if (nickname == null) {
            nickname = "user_" + providerId;
        }
        return new String[]{providerId, nickname};
    }
}
