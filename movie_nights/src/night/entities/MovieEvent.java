package night.entities;

import com.google.api.client.util.DateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document
public class MovieEvent {

    @Id
    private String id;
    private String creatorId;
    private List<String> usersToInvite;
    private String title;
    private String description;
    private DateTime startDate;
    private DateTime endDate;
    private String location;

    public MovieEvent() { }

    public MovieEvent(String id, List<String> usersToInvite, String title, long startDate, long endDate, String creatorId, String description, String location) {
        this.id = id;
        this.usersToInvite = usersToInvite;
        this.title = title;
        this.startDate = new DateTime(startDate);
        this.creatorId = creatorId;
        this.description = description;
        this.endDate = new DateTime(endDate);
        this.location = location;
    }

    public String getId() {
        return id;
    }

    public List<String> getUsersToInvite() {
        return usersToInvite;
    }

    public String getTitle() {
        return title;
    }

    public DateTime getStartDate() {
        return startDate;
    }

    public DateTime getEndDate() {
        return endDate;
    }

    public String getLocation() {
        return location;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public String getDescription() {
        return description;
    }

    public void setUsersToInvite(List<String> usersToInvite) {
        this.usersToInvite = usersToInvite;
    }
}