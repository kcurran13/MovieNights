package night.security;

import io.jsonwebtoken.Claims;
import night.Repositories.UserRepository;
import night.Repositories.WhitelistRepository;
import night.entities.DbUser;
import night.security.entities.AuthRequest;
import night.security.entities.AuthResponse;
import night.security.entities.Whitelist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    @Resource(name = "authenticationManager")
    private AuthenticationManager authManager;
    @Autowired
    private UserRepository dbUserRepository;
    @Autowired
    private WhitelistRepository whitelistRepository;
    @Autowired
    private JwtUtil jwtUtil;

    @Value("${jwt.refresh.expiration.seconds}")
    private int REFRESH_TOKEN_EXPIRATION;
    @Value("${jwt.refresh.secret}")
    private String REFRESH_TOKEN_SECRET;
    @Value("${jwt.expiration.seconds}")
    private int TOKEN_EXPIRATION;
    @Value("${jwt.secret}")
    private String TOKEN_SECRET;

    @PostMapping("/login")
    private ResponseEntity<AuthResponse> login(@RequestBody AuthRequest authRequest, HttpServletResponse response) {
        final DbUser dbUser = dbUserRepository.findDistinctFirstByUsernameIgnoreCase(authRequest.getUsername());
        final String jwt = jwtUtil.createToken(
                new HashMap<>(Map.of("roles", dbUser.getRoles().toArray(String[]::new))),
                dbUser.getId(),
                TOKEN_SECRET,
                TOKEN_EXPIRATION
        );

        final String refreshJwt = jwtUtil.createToken(
                new HashMap<>(),
                dbUser.getId(),
                REFRESH_TOKEN_SECRET,
                REFRESH_TOKEN_EXPIRATION
        );

        try {
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));
        } catch (BadCredentialsException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "BAD CREDENTIALS");
        }

        if (dbUser == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User does not exist.");

        whitelistRepository.save(new Whitelist(dbUser.getId(), refreshJwt));
        response.addCookie(jwtUtil.createCookieForRefreshToken("refreshToken", refreshJwt, REFRESH_TOKEN_EXPIRATION));

        return new ResponseEntity<>(new AuthResponse(jwt, dbUser), HttpStatus.OK);
    }

    @DeleteMapping("/logout")
    private ResponseEntity<?> logout(@CookieValue("refreshToken") String refreshToken, HttpServletResponse response) {
        //        Claims claims = jwtUtil.extractAllClaims(refreshToken, REFRESH_TOKEN_SECRET);
        jwtUtil.extractAllClaims(refreshToken, REFRESH_TOKEN_SECRET);
        whitelistRepository.deleteByToken(refreshToken);
        response.addCookie(jwtUtil.createCookieForRefreshToken("refreshToken", "", 0));

        return new ResponseEntity<>("Logged out", HttpStatus.OK);
    }

    @PostMapping("/refresh_token")
    private ResponseEntity<AuthResponse> refreshToken(@CookieValue("refreshToken") String refreshToken, HttpServletResponse response) {
        final Claims tokenClaims = jwtUtil.extractAllClaims(refreshToken, REFRESH_TOKEN_SECRET);
        final String userId = tokenClaims.getSubject();
        final DbUser dbUser = dbUserRepository.findDistinctById(userId);
        final Whitelist whitelistItem = whitelistRepository.findDistinctFirstByToken(refreshToken);
        final String jwt = jwtUtil.createToken(
                new HashMap<>(Map.of("roles", dbUser.getRoles().toArray(String[]::new))),
                dbUser.getId(),
                TOKEN_SECRET,
                TOKEN_EXPIRATION
        );
        final String refreshJwt = jwtUtil.createToken(
                new HashMap<>(),
                dbUser.getId(),
                REFRESH_TOKEN_SECRET,
                REFRESH_TOKEN_EXPIRATION
        );

        if (refreshToken == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token not refreshed.");

        if (!jwtUtil.isTokenValid(refreshToken, REFRESH_TOKEN_SECRET))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token.");

        if (dbUser == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User does not exist");

        if (whitelistItem == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");

        whitelistRepository.delete(whitelistItem);
        whitelistRepository.save(new Whitelist(dbUser.getId(), refreshJwt));
        response.addCookie(jwtUtil.createCookieForRefreshToken("refreshToken", refreshJwt, REFRESH_TOKEN_EXPIRATION));

        return new ResponseEntity<>(new AuthResponse(jwt, dbUser), HttpStatus.OK);
    }

    @GetMapping("/active_user")
    private ResponseEntity<DbUser> getActiveUser(HttpServletRequest request) {
        final String jwt = jwtUtil.getJwtFromRequest(request);
        final Claims claims = jwtUtil.extractAllClaims(jwt, TOKEN_SECRET);
        final String userId = claims.getSubject();
        DbUser dbUser = dbUserRepository.findDistinctById(userId);

        if (!jwtUtil.isTokenValid(jwt, TOKEN_SECRET))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");

        if (dbUser != null) {
            return new ResponseEntity<>(dbUser, HttpStatus.OK);
        }
        throw new ResponseStatusException(HttpStatus.NO_CONTENT, "Active user do not exist");
    }
}

