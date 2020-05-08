package night.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

public class OMDBSearchResult {
    @JsonProperty("Search")
    private ArrayList<Movie> search;
    @JsonProperty("totalResults")
    private int totalResults;
    @JsonProperty("Response")
    private Boolean response;
    private String query;

    public OMDBSearchResult() { }

    public OMDBSearchResult(ArrayList<Movie> search, int totalResults, Boolean response, String query) {
        this.search = search;
        this.totalResults = totalResults;
        this.response = response;
        this.query = query;
    }

    public ArrayList<Movie> getSearch() {
        return search;
    }

    public void setSearch(ArrayList<Movie> search) {
        this.search = search;
    }

    public int getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(int totalResults) {
        this.totalResults = totalResults;
    }

    public Boolean getResponse() {
        return response;
    }

    public void setResponse(Boolean response) {
        this.response = response;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
}