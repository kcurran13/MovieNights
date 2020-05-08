package night.security;

import io.jsonwebtoken.Claims;
import night.Services.MyUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private MyUserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${jwt.secret}")
    private String TOKEN_SECRET;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String jwt = jwtUtil.getJwtFromRequest(request);

        if(SecurityContextHolder.getContext().getAuthentication() == null){
            if (jwtUtil.isTokenValid(jwt,TOKEN_SECRET)){
                Claims claims = jwtUtil.extractAllClaims(jwt,TOKEN_SECRET);
                final String[] authorities =
                        Arrays.stream(claims.get("roles").toString().replaceAll("\\[|\\]|\\s","").split(","))
                                .map(String::new)
                                .toArray(String[]::new);
                UserDetails myUserDetails = userDetailsService.loadUserById(claims.getSubject(),authorities);
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                            myUserDetails, null, myUserDetails.getAuthorities());
                usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }
        }
        filterChain.doFilter(request,response);
    }
}
