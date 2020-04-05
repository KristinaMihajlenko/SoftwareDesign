import com.mongodb.client.model.Filters
import com.mongodb.rx.client.MongoClient
import com.mongodb.rx.client.MongoClients
import com.mongodb.rx.client.Success

import org.bson.Document
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.TimeUnit

class MongoDriver(private val databaseName: String) {
    val events: ConcurrentLinkedQueue<Event> = ConcurrentLinkedQueue()

    fun addTicket(ticket: Ticket): Success {
        return client.getDatabase(databaseName).getCollection("ticket").insertOne(ticket.document)
                .timeout(15, TimeUnit.SECONDS).toBlocking().single()
    }

    fun addEvent(event: Event): Success {
        val result = client.getDatabase(databaseName).getCollection("event").insertOne(event.document)
                .timeout(15, TimeUnit.SECONDS).toBlocking().single()
        if (result == Success.SUCCESS) {
            events.add(event)
        }
        return result
    }

    fun getLatestTicketVersion(id: Int?): Ticket? {

            return getAllTicketVersions(id)
                    .stream()
                    .max(Comparator.comparing { obj: Ticket? -> obj?.creationDate })
                    .orElse(null)

    }

    fun getAllTicketVersions(id: Int?): List<Ticket?> {
        return client.getDatabase(databaseName).getCollection("ticket")
                .find(Filters.eq("id", id))
                .toObservable()
                .map { doc -> Ticket(doc) }
                .toList()
                .toBlocking()
                .single();
    }

    companion object {
        val client: MongoClient = MongoClients.create("mongodb://localhost:27017")
    }

    init {
        client.getDatabase(databaseName).getCollection("event").find().maxTime(10, TimeUnit.SECONDS)
                .toObservable().map { document: Document? -> Event(document!!) }
                .toBlocking().subscribe { e: Event -> events.add(e) }
    }
}

