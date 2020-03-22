import actor.ChildActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.util.Timeout;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import search.SearchSystem;
import search.SearchRequest;
import search.SearchResult;
import search.SearchClientStub;

import java.util.concurrent.TimeUnit;

import static akka.pattern.PatternsCS.ask;
import static org.assertj.core.api.Assertions.assertThat;

public class ChildActorTest {
    private ActorSystem system;

    @Before
    public void setUp() {
        system = ActorSystem.create("ChildActorTest");
    }

    @After
    public void tearDown() {
        system.terminate();
    }

    @Test
    public void testChildActor() {
        ActorRef childActor = system.actorOf(Props.create(ChildActor.class, new SearchClientStub(0)));

        SearchResult response = (SearchResult) ask(
                childActor,
                new SearchRequest(SearchSystem.YANDEX, "query"),
                Timeout.apply(10, TimeUnit.SECONDS)
        ).toCompletableFuture().join();


        assertThat(response.getSearchSystemResults()).isNotEmpty();
    }
}
