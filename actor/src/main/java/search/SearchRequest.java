package search;

public class SearchRequest {
    private final SearchSystem searchSystem;
    private final String queryText;

    public SearchRequest(SearchSystem searchSystem, String queryText) {
        this.searchSystem = searchSystem;
        this.queryText = queryText;
    }

    public SearchSystem getSearchSystem() {
        return searchSystem;
    }

    public String getQueryText() {
        return queryText;
    }
}
