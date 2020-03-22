package search;

import java.util.List;

public class SearchResult {
    private final SearchSystem searchSystem;
    private final List<SearchResultItem> searchSystemResults;

    public SearchResult(SearchSystem searchSystem, List<SearchResultItem> searchSystemResults) {
        this.searchSystem = searchSystem;
        this.searchSystemResults = searchSystemResults;
    }

    public SearchSystem getSearchSystem() {
        return searchSystem;
    }

    public List<SearchResultItem> getSearchSystemResults() {
        return searchSystemResults;
    }
}