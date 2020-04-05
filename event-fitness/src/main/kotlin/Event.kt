import org.bson.Document
import java.util.*

class Event(val ticketId: Int, val time: Date, val eventType: EventType) {
    enum class EventType {
        ENTER, EXIT
    }

    constructor(document: Document) : this(document.getInteger("ticketId"),
            document.getDate("time"),
            EventType.valueOf(document.getString("eventType").toUpperCase())) {
    }

    override fun equals(other: Any?): Boolean {
        if (other == null) {
            return false
        }
        if (other === this) {
            return true
        }
        if (other is Event) {
            return ticketId == other.ticketId && time == other.time && eventType == other.eventType
        }
        return false
    }

    val document: Document
        get() = Document("ticketId", ticketId).append("time", time).append("eventType", eventType.toString())

}