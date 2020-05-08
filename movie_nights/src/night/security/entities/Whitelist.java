package night.security.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document
public class Whitelist {

    @Id
    private String id;
    private String userId;
    private String token;
    private Date createdAt;

    public Whitelist(String userId, String token) {
        this.userId = userId;
        this.token = token;
        this.createdAt = new Date(System.currentTimeMillis());
    }

    public String getUserId() {
        return userId;
    }

    public String getToken() {
        return token;
    }

    public String getId() {
        return id;
    }
}
