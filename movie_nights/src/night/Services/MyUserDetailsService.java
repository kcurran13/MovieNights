package night.Services;

import night.Repositories.UserRepository;
import night.entities.DbUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.annotation.PostConstruct;
import java.util.List;

@Configuration
public class MyUserDetailsService implements UserDetailsService {

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public BCryptPasswordEncoder getEncoder() { return encoder; }

    @Autowired
    private UserRepository repository;

    @PostConstruct
    private void createDefaultUsers() {
        if (repository.findDistinctFirstByUsernameIgnoreCase("user") == null) {
            addUser("user", "password");
        }
        if (repository.findDistinctFirstByUsernameIgnoreCase("test") == null) {
            addUser("test", "password");
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        DbUser dbUser = repository.findDistinctFirstByUsernameIgnoreCase(username);
        if (dbUser == null) {
            throw new UsernameNotFoundException(username + " not found.");
        }
        return toUserDetails(dbUser);
    }

    public void addUser(String name, String password) {
        DbUser user = new DbUser(name, encoder.encode(password), List.of("ADMIN", "USER"));
        try {
            repository.save(user);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private UserDetails toUserDetails(DbUser dbUser) {
        return org.springframework.security.core.userdetails.User
                .withUsername(dbUser.getUsername())
                .password(dbUser.getPassword())
                .roles(dbUser.getRoles().toArray(String[]::new)).build();
    }

    public UserDetails loadUserById(String userId, String[] userRoles) {
        return org.springframework.security.core.userdetails.User
                .withUsername(userId)
                .password("")
                .roles(userRoles).build();
    }

    public static UserDetails getCurrentUserDetails() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();
        if (authentication != null) {
            Object principal = authentication.getPrincipal();
            return principal instanceof UserDetails ? (UserDetails) principal : null;
        }
        return null;
    }
}