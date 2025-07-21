package uz.pdp.online_education.config.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import uz.pdp.online_education.model.User; // Sizning User klassingiz
import uz.pdp.online_education.config.properties.JwtProperties;

import java.security.Key;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties jwtProperties;
    private Key signingKey;

    @PostConstruct
    public void init() {
        // Secret kalitni faqat bir marta, servis ishga tushganda yaratib olamiz.
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    // --- Token generatsiya qilish ---

    /**
     * Foydalanuvchi ma'lumotlari asosida Access Token yaratadi.
     */
    public String generateAccessToken(UserDetails userDetails) {
        Map<String, Object> extraClaims = new HashMap<>();
        // Tokenga foydalanuvchining rollarini qo'shamiz
        extraClaims.put("roles", userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList());

        // Agar sizning User klassingizda qo'shimcha ma'lumotlar bo'lsa, ularni ham qo'shishingiz mumkin
        if (userDetails instanceof User user) {
            extraClaims.put("userId", user.getId());
            extraClaims.put("profileId",user.getProfile().getId());
        }

        return buildToken(extraClaims, userDetails, jwtProperties.getAccessTokenExpiration());
    }

    /**
     * Foydalanuvchi ma'lumotlari asosida Refresh Token yaratadi.
     * Refresh token odatda qo'shimcha ma'lumotlarsiz bo'ladi.
     */
    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(new HashMap<>(), userDetails, jwtProperties.getRefreshTokenExpiration());
    }

    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            Duration expiration
    ) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration.toMillis());

        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // --- Token ma'lumotlarini o'qish (Extraction) ---

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // --- Tokenni tekshirish (Validation) ---

    /**
     * Tokenning yaroqliligini (validligini) tekshiradi.
     * Username to'g'ri kelishi va tokenning muddati o'tmagan bo'lishi kerak.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    /**
     * Tokenning yashash muddati o'tgan yoki o'tmaganligini tekshiradi.
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}