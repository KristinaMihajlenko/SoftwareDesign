import com.mongodb.rx.client.Success
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import rx.observers.TestSubscriber
import java.util.*
import java.util.concurrent.TimeUnit

class ReportServerTest {
    var mongoDriver: MongoDriver? = null
    var server: ReportServer? = null
    @Before
    fun clearDB() {
        val subscriber = TestSubscriber<Success>()
        MongoDriver.client.getDatabase(DATABASE_NAME).drop().subscribe(subscriber)
        subscriber.awaitTerminalEvent()
        mongoDriver = MongoDriver(DATABASE_NAME)
        server = ReportServer(mongoDriver!!.events)
    }

    @Test
    fun testEmpty() {
        Assert.assertEquals("no records", server!!.dailyEntryCount())
        Assert.assertEquals("no records", server!!.meanVisitTime())
    }

    @Test
    fun testMediumDuration() {
        val calendar = Calendar.getInstance()
        calendar[2014, Calendar.JUNE] = 23
        val creation = calendar.time
        val expiration = Date(creation.time + TimeUnit.MILLISECONDS.convert(365, TimeUnit.DAYS))
        mongoDriver!!.addTicket(Ticket(0, creation, expiration))
        var entry = Date(creation.time + TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS))
        var exit = Date(entry.time + TimeUnit.MILLISECONDS.convert(2, TimeUnit.HOURS))
        mongoDriver!!.addEvent(Event(0, entry, Event.EventType.ENTER))
        mongoDriver!!.addEvent(Event(0, exit, Event.EventType.EXIT))
        entry = Date(creation.time + TimeUnit.MILLISECONDS.convert(3, TimeUnit.DAYS))
        exit = Date(entry.time + TimeUnit.MILLISECONDS.convert(3, TimeUnit.HOURS))
        mongoDriver!!.addEvent(Event(0, entry, Event.EventType.ENTER))
        mongoDriver!!.addEvent(Event(0, exit, Event.EventType.EXIT))
        Assert.assertEquals("9000000 MILLISECONDS", server!!.meanVisitTime())
        Assert.assertEquals("24-06-2014 1\n26-06-2014 1\n", server!!.dailyEntryCount())
    }

    companion object {
        private const val DATABASE_NAME = "reporttest"
    }
}