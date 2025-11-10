package util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import model.User;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.github.cdimascio.dotenv.Dotenv;

public class JwtUtil {
    private static final Dotenv dotenv = Dotenv.load();

    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(dotenv.get("JWT_KEY").getBytes(StandardCharsets.UTF_8));
    private static final long EXPIRATION_TIME = Long.parseLong(dotenv.get("JWT_EXPIRATION_HOURS", "2")) * 1000 * 60 * 60;

    public static String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getId());
        claims.put("email", user.getEmail());
        claims.put("role", user.getRole().name());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getEmail())
                .setIssuedAt(new Date())
                .setIssuer("HotelReservationSystem")
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    public static boolean validateToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            Date expiration = claims.getExpiration();
            return expiration != null && expiration.after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    public static Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public static String extractEmail(String token) {
        return extractAllClaims(token).get("email", String.class);
    }

    public static String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    public static int extractUserId(String token) {
        Object idObj = extractAllClaims(token).get("id");
        if (idObj instanceof Integer) {
            return (int) idObj;
        }
        if (idObj instanceof Number) {
            return ((Number) idObj).intValue();
        }
        return -1;
    }

    public static String refreshToken(String token) {
        Claims claims = extractAllClaims(token);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(claims.getSubject())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
    }
}
