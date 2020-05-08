package night.entities;

import org.springframework.data.annotation.Id;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class GoogleToken {
    @Id
    private String id;
    @NotNull
    private String userId;
    @NotEmpty
    private String accessToken;
    @NotEmpty
    private String refreshToken;
    @NotEmpty
    private Long expiresAt;
    @NotEmpty
    private String googleUserId;
    @NotEmpty
    private String gmail;


    public GoogleToken() { }

    public GoogleToken(String userId, String accessToken, String refreshToken, Long expiresAt, String googleUserId, String gmail) {
        this.userId = userId;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresAt = expiresAt;
        this.googleUserId = googleUserId;
        this.gmail = gmail;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    public Long getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Long expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getGoogleUserId() { return googleUserId; }

    public void setGoogleUserId(String googleUserId) { this.googleUserId = googleUserId; }

    public String getGmail() {
        return gmail;
    }

    public void setGmail(String gmail) { this.gmail = gmail; }
}