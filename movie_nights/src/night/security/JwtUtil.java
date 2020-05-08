package night.security;

import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Map;

@Service
public class JwtUtil {

    @Value("${jwt.secret}")
    private String TOKEN_SECRET;
    @Value("${jwt.refresh.secret}")
    private String REFRESH_TOKEN_SECRET;

    public Claims extractAllClaims(String token, String secret) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    }

    public String createToken(Map<String, Object> claims, String subject, String token_secret, int expiresInSeconds) {
        final long expiresInMillis = (long) expiresInSeconds * 1000;
        final long expirationDate = System.currentTimeMillis() + expiresInMillis;
        return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(expirationDate))
                .signWith(SignatureAlgorithm.HS256, token_secret).compact();
    }

    public Cookie createCookieForRefreshToken(String cookieName, String token, int maxAge) {
        Cookie cookie = new Cookie(cookieName, token);
        cookie.setMaxAge(maxAge);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");

        return cookie;
    }

    public boolean isTokenValid(String token, String secret) {
        try {
            Jwts.parser().setSigningKey(secret).parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException | UnsupportedJwtException | MalformedJwtException | SignatureException | IllegalArgumentException err) {
            return false;
        }
    }

    public String getJwtFromRequest(HttpServletRequest request) {
        final String authorisationHeader = request.getHeader("Authorization");

        if (authorisationHeader != null && authorisationHeader.startsWith("Bearer ")) {
            return authorisationHeader.substring(7);
        }
        return null;
    }
}
