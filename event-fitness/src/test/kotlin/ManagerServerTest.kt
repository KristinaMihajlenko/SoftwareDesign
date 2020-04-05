import com.mongodb.rx.client.Success
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import rx.observers.TestSubscriber
import java.util.*

class ManagerServerTest {
    var mongoDriver: MongoDriver? = null
    var server: ManagerServer? = null
    @Before
    fun clearDB() {
        val subscriber = TestSubscriber<Success>()
        MongoDriver.client.getDatabase(DATABASE_NAME).drop().subscribe(subscriber)
        subscriber.awaitTerminalEvent()
        mongoDriver = MongoDriver(DATABASE_NAME)
        server = ManagerServer(mongoDriver!!)
    }

    @Test
    @Throws(Throwable::class)
    fun testManger() {
        val queryParam: MutableMap<String?, List<String?>?> = HashMap()
        queryParam["id"] = listOf("0")
        queryParam["creationDate"] = listOf("23-01-1998")
        queryParam["expirationDate"] = listOf("23-03-1998")
        server!!.addTicket(queryParam)
        queryParam.replace("id", listOf("0"))
        queryParam["creationDate"] = listOf("23-02-1998")
        queryParam["expirationDate"] = listOf("23-04-1998")
        server!!.addTicket(queryParam)
        val tickets = mongoDriver!!.getAllTicketVersions(0)
        Assert.assertEquals(2, tickets.size.toLong())
        val ticket = tickets[1]
        Assert.assertEquals(0, ticket!!.id.toLong())
        Assert.assertEquals("Mon Feb 23 00:00:00 MSK 1998", ticket.creationDate.toString())
        Assert.assertEquals("Thu Apr 23 00:00:00 MSD 1998", ticket.expirationDate.toString())
    }

    companion object {
        private const val DATABASE_NAME = "test"
    }
}