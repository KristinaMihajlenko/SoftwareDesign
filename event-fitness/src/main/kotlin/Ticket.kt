import org.bson.Document
import java.util.*

class Ticket(val id: Int, val creationDate: Date, val expirationDate: Date) {

    constructor(document: Document) : this(document.getInteger("id"),
            document.getDate("creationDate"),
            document.getDate("expirationDate")) {
    }

    val document: Document
        get() = Document("id", id).append("creationDate", creationDate).append("expirationDate", expirationDate)

    override fun toString(): String {
        return "id=$id,created = $creationDate, expired=$expirationDate"
    }

}