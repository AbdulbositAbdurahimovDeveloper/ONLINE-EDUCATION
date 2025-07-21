package uz.pdp.online_education.config.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Data
@Component
@ConfigurationProperties(prefix = "application.jwt")
public class JwtProperties {
    private String secret;
    private Duration accessTokenExpiration;
    private Duration refreshTokenExpiration;
}