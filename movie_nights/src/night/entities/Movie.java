package night.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Movie {
    @JsonProperty("Title")
    private String title;
    @JsonProperty("imdbID")
    private String imdbID;
    @JsonProperty("Type")
    private String type;

    public Movie() { }

    public Movie(String title, String imdbID, String type) {
        this.title = title;
        this.imdbID = imdbID;
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImdbID() {
        return imdbID;
    }

    public void setImdbID(String imdbID) {
        this.imdbID = imdbID;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}