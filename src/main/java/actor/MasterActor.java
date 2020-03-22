package actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import scala.concurrent.duration.FiniteDuration;
import search.SearchClient;
import search.SearchRequest;
import search.SearchResult;
import search.SearchSystem;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;


public class MasterActor extends AbstractActor {
    private static final Set<SearchSystem> SEARCH_SYSTEMS = EnumSet.allOf(SearchSystem.class);
    private static final int TIMEOUT_PERIOD = 1;

    private static final TimeoutMsg TIMEOUT_MSG = new TimeoutMsg();
    private static final StopMsg STOP_MSG = new StopMsg();

    private final NameFactory nameFactory;
    private ActorRef requestSender;

    private List<SearchResult> responseList;

    private final SearchClient searchClient;

    public MasterActor(SearchClient searchClient) {
        super();
        this.searchClient = searchClient;
        this.nameFactory = new NameFactory();
        this.responseList = new ArrayList<>();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class, request -> {
                    this.requestSender = getSender();

                    SEARCH_SYSTEMS.forEach(searchSystems -> getContext()
                            .actorOf(
                                    Props.create(ChildActor.class, searchClient),
                                    nameFactory.nextName()
                            ).tell(new SearchRequest(searchSystems, request), self())
                    );

                    context().system().scheduler()
                            .scheduleOnce(
                                    FiniteDuration.apply(TIMEOUT_PERIOD, TimeUnit.SECONDS),
                                    self(),
                                    TIMEOUT_MSG,
                                    context().system().dispatcher(),
                                    ActorRef.noSender()
                            );
                }).match(SearchResult.class, response -> {
                    responseList.add(response);

                    if (responseList.size() == SEARCH_SYSTEMS.size()) {
                        sendSearchResult();
                    }
                }).match(TimeoutMsg.class, ignored -> sendSearchResult())
                .match(StopMsg.class, ignored -> getContext().stop(this.self()))
                .build();
    }

    private void sendSearchResult() {
        requestSender.tell(responseList, self());
        self().tell(STOP_MSG, self());
    }

    private static class NameFactory {

        private int count = 0;

        public String nextName() {
            return "child" + count++;
        }
    }

    private static final class TimeoutMsg {
    }

    private static final class StopMsg {
    }
}
