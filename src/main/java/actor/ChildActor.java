package actor;

import akka.actor.AbstractActor;
import search.SearchClient;
import search.SearchRequest;

public class ChildActor extends AbstractActor {
    private final SearchClient searchClient;

    public ChildActor(SearchClient searchClient) {
        super();
        this.searchClient = searchClient;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(SearchRequest.class, searchRequest ->
                        getSender().tell(searchClient.searchQuery(searchRequest), getSender())
                ).build();
    }
}
