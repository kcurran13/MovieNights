package night.controllers;

import night.Repositories.UserRepository;
import night.Services.MyGoogleService;
import night.Services.MyUserDetailsService;
import night.entities.DbUser;
import night.entities.GoogleToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    MyUserDetailsService myUserDetailsService;

    @Autowired
    private UserRepository repo;

    @Autowired
    private MyGoogleService myGoogleService;

    @GetMapping
    ResponseEntity<Iterable> getUsers() {
        Iterable users = repo.findAll();

        if (((List) users).isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(users, HttpStatus.OK);
        }
    }

    @GetMapping("/connectedToGoogle")
    ResponseEntity<Iterable> getUsersConnectedToGoogle() {
        List<GoogleToken> tokens = myGoogleService.getAllGoogleTokens();
        List<DbUser> users = new ArrayList<DbUser>();
        Optional<DbUser> user;

        for (GoogleToken token : tokens) {
            user = repo.findById(token.getUserId());
            user.ifPresent(users::add);
        }
        if (users.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(users, HttpStatus.OK);
        }
    }

    @GetMapping("{id}")
    ResponseEntity<Optional<DbUser>> getUser(@PathVariable String id) {
        Optional<DbUser> user = repo.findById(id);

        if (user.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);

        } else {
            return new ResponseEntity<>(user, HttpStatus.OK);
        }
    }

    @GetMapping("/name/{name}")
    ResponseEntity<Optional<DbUser>> getNamedUser(@PathVariable String name) {
        Optional<DbUser> user = repo.findByUsername(name);

        if (user.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(user, HttpStatus.OK);
        }
    }

    @PostMapping
    ResponseEntity<DbUser> addUser(@RequestBody @Valid DbUser body) {
        BCryptPasswordEncoder encoder = myUserDetailsService.getEncoder();
        DbUser newDbUser;

        if (isAdmin() && body.getRoles().size() > 0) {
            newDbUser = new DbUser(body.getUsername(), encoder.encode(body.getPassword()), body.getRoles());
        } else {
            newDbUser = new DbUser(body.getUsername(), encoder.encode(body.getPassword()), List.of("USER"));
        }

        try {
            repo.save(newDbUser);
            return new ResponseEntity<>(newDbUser, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public static Boolean validateRoles(List<String> roles) {
        if (roles.stream().anyMatch(role -> role.equals("USER"))) {
            return true;
        } else return roles.stream().anyMatch(role -> role.equals("ADMIN"));
    }

    public static UserDetails currentUserDetails() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();

        if (authentication != null) {
            Object principal = authentication.getPrincipal();
            return principal instanceof UserDetails ? (UserDetails) principal : null;
        }
        return null;
    }

    public static boolean isAdmin() {
        return currentUserDetails() != null && currentUserDetails().getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
    }

    @PostMapping("/storeauthcode")
    public ResponseEntity storeauthcode(@RequestBody Map<String, String> body, @RequestHeader("X-Requested-With") String encoding) {
        String response = myGoogleService.storeauthcode(body.get("code"), body.get("userId"));

        if (encoding == null || encoding.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (response.equals("OK")) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
