package night.security.entities;

import night.entities.DbUser;

public class AuthResponse {

    private final String jwt;
    private final DbUser dbUser;

    public AuthResponse(String jwt, DbUser dbUser) {
        this.jwt = jwt;
        this.dbUser = dbUser;
    }

    public String getJwt() {
        return jwt;
    }

    public DbUser getDbUser() {
        return dbUser;
    }
}
