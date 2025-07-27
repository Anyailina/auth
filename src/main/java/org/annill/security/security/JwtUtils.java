package org.annill.security.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.annill.security.dto.UserDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Утилита для работы с JWT токенами. Поддерживает генерацию, валидацию и извлечение данных из токенов.
 */
@Slf4j
@Component
public class JwtUtils {

    @Value("${spring.jwt.jwtSecret}")
    private String jwtSecret;

    @Value("${spring.jwt.jwtExpirationMs}")
    private int jwtExpirationMs;

    /**
     * Генерирует JWT токен для пользователя.
     *
     * @param userDto DTO пользователя
     * @return сгенерированный токен
     */
    public String generateJwtToken(UserDto userDto) {

        List<String> roles = new ArrayList<>();
        userDto.getRoles().forEach(r -> {
            roles.add(r.getName().toString());
        });

        return Jwts.builder()
            .subject(userDto.getUsername())
            .claim("roles", roles)
            .issuedAt(new Date())
            .expiration(new Date((new Date()).getTime() + jwtExpirationMs))
            .signWith(key(), SignatureAlgorithm.HS256)
            .compact();
    }

    public List<String> getRoles(String token) {
        Claims claims = Jwts.parser().setSigningKey(key()).build()
            .parseClaimsJws(token).getBody();
        List<String> list = claims.get("roles", List.class);
        return list;
    }

    private Key key() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Извлекает имя пользователя из токена.
     *
     * @param token JWT токен
     * @return имя пользователя
     */
    public String getUserNameFromJwtToken(String token) {
        return Jwts.parser().setSigningKey(key()).build()
            .parseClaimsJws(token).getBody().getSubject();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(key()).build().parse(authToken);
            return true;
        } catch (MalformedJwtException e) {
            log.error("Невалидный JWT: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT срок действия окончен: {}", e.getMessage());
        }

        return false;
    }

}
