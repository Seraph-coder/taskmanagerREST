package ru.naujava.taskmanager.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Утилитный класс для работы с JWT токенами.
 * Предоставляет методы для генерации, валидации и извлечения информации из JWT токенов.
 */
@Component
public class JwtUtil {
    @Value("${jwt.secret:defaultSecret123023404560789012345678901234567890}")
    private String secret;

    @Value("${jwt.expiration:86400000}")
    private Long expiration;

    /**
     * Получает ключ для подписи токенов.
     *
     * @return ключ для подписи
     */
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * Генерирует JWT токен для пользователя.
     *
     * @param userDetails детали пользователя
     * @return сгенерированный JWT токен
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername());
    }

    /**
     * Создает JWT токен с указанными claims и subject.
     *
     * @param claims дополнительные claims
     * @param subject субъект токена
     * @return сгенерированный токен
     */
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Валидирует JWT токен для пользователя.
     *
     * @param token JWT токен
     * @param userDetails детали пользователя
     * @return true если токен валиден, иначе false
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    /**
     * Извлекает имя пользователя из JWT токена.
     *
     * @param token JWT токен
     * @return имя пользователя
     */
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * Извлекает дату истечения из JWT токена.
     *
     * @param token JWT токен
     * @return дата истечения
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     * Извлекает claim из JWT токена.
     *
     * @param token JWT токен
     * @param claimsResolver функция для извлечения claim
     * @param <T> тип claim
     * @return извлеченный claim
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Извлекает все claims из JWT токена.
     *
     * @param token JWT токен
     * @return claims
     */
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Проверяет, истек ли JWT токен.
     *
     * @param token JWT токен
     * @return true если токен истек, иначе false
     */
    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }
}
