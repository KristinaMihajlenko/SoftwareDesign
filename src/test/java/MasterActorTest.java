

import actor.MasterActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.util.Timeout;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import search.SearchClientStub;
import search.SearchResult;
import search.SearchSystem;

import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static akka.pattern.PatternsCS.ask;
import static org.assertj.core.api.Assertions.assertThat;

public class MasterActorTest {
    private ActorSystem system;

    @Before
    public void setUp() {
        system = ActorSystem.create("MasterActorTest");
    }

    @After
    public void tearDown() {
        system.terminate();
    }

    @Test
    public void testMasterActor() {
        ActorRef masterActor = system.actorOf(Props.create(MasterActor.class, new SearchClientStub(0)));

        List<SearchResult> response = (List<SearchResult>) ask(
                masterActor,
                "query",
                Timeout.apply(10, TimeUnit.SECONDS)
        ).toCompletableFuture().join();

        assertThat(response.size()).isEqualTo(EnumSet.allOf(SearchSystem.class).size());
        assertThat(response.get(0).getSearchSystemResults()).isNotEmpty();
    }

    @Test
    public void testMasterActorTimeout() {
        ActorRef masterActor = system.actorOf(Props.create(MasterActor.class, new SearchClientStub(2000)));

        List<SearchResult> response = (List<SearchResult>) ask(
                masterActor,
                "query",
                Timeout.apply(10, TimeUnit.SECONDS)
        ).toCompletableFuture().join();

        assertThat(response).isEmpty();

    }
}
