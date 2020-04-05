import com.mongodb.rx.client.Success
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import rx.observers.TestSubscriber
import java.util.*

class TurnstileServerTest {
    var mongoDriver: MongoDriver? = null
    var server: TurnstileServer? = null
    @Before
    fun clearDB() {
        val subscriber = TestSubscriber<Success>()
        MongoDriver.client.getDatabase(DATABASE_NAME).drop().subscribe(subscriber)
        subscriber.awaitTerminalEvent()
        mongoDriver = MongoDriver(DATABASE_NAME)
        server = TurnstileServer(mongoDriver!!)
    }

    @Test
    fun testEnterExit() {
        val queryParam: MutableMap<String?, List<String?>?> = HashMap()
        queryParam["id"] = listOf("0")
        val subscriber = TestSubscriber<String>()
        server!!.enter(queryParam, Date()).subscribe(subscriber)
        subscriber.assertValues("no tickers")
        val creation = Date()
        val enterTime = Date(creation.time + 1)
        val exitTime = Date(creation.time + 2)
        val expiration = Date(creation.time + 4)
        mongoDriver!!.addTicket(Ticket(0, creation, expiration))
        val subscriberEnter = TestSubscriber<String>()
        server!!.enter(queryParam, enterTime).subscribe(subscriberEnter)
        subscriberEnter.assertValue("enter")
        subscriberEnter.awaitTerminalEvent()
        val subscriberExit = TestSubscriber<String>()
        server!!.exit(queryParam, exitTime).subscribe(subscriberExit)
        subscriberExit.assertValue("exit")
        subscriberExit.awaitTerminalEvent()
        val enterE = mongoDriver!!.events.toTypedArray()[0] as Event
        val exitE = mongoDriver!!.events.toTypedArray()[1] as Event
        Assert.assertEquals(Event.EventType.ENTER, enterE.eventType)
        Assert.assertEquals(Event.EventType.EXIT, exitE.eventType)
        Assert.assertEquals(enterTime, enterE.time)
        Assert.assertEquals(exitTime, exitE.time)
        Assert.assertEquals(0, enterE.ticketId.toLong())
        Assert.assertEquals(0, exitE.ticketId.toLong())
    }

    companion object {
        private const val DATABASE_NAME = "turnstiletest"
    }
}