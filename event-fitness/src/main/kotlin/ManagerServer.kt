import com.mongodb.rx.client.Success
import io.netty.buffer.ByteBuf
import io.netty.handler.codec.http.HttpResponseStatus
import io.reactivex.netty.protocol.http.server.HttpServer
import io.reactivex.netty.protocol.http.server.HttpServerRequest
import io.reactivex.netty.protocol.http.server.HttpServerResponse
import rx.Observable
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class ManagerServer(private val mongoDriver: MongoDriver) {
    fun run() {
        HttpServer.newServer(8081).start { req: HttpServerRequest<ByteBuf?>, resp: HttpServerResponse<ByteBuf?> ->
            val response: Observable<String>
            val action = req.decodedPath.substring(1)
            val queryParam = req.queryParameters
            when (action) {
                "get" -> {
                    response = getTicket(queryParam)
                    resp.setStatus(HttpResponseStatus.OK)
                }
                "add" -> {
                    response = addTicket(queryParam)
                    resp.setStatus(HttpResponseStatus.OK)
                }
                else -> {
                    response = Observable.just("bad request")
                    resp.setStatus(HttpResponseStatus.BAD_REQUEST)
                }
            }
            resp.writeString(response)
        }.awaitShutdown()
    }

    private fun getTicket(queryParam: Map<String?, List<String?>?>): Observable<String> {
        val id = (queryParam["id"] ?: error(""))[0]!!.toInt()
        val ticket = mongoDriver.getLatestTicketVersion(id)
        return if (ticket == null) {
            Observable.just("no ticket")
        } else {
            Observable.just(ticket.toString())
        }
    }

    fun addTicket(queryParam: Map<String?, List<String?>?>): Observable<String> {
        val format = SimpleDateFormat("dd-MM-yyyy")
        val created: Date
        val expired: Date
        try {
            created = format.parse(queryParam["creationDate"]!![0])
            expired = format.parse(queryParam["expirationDate"]!![0])
        } catch (e: ParseException) {
            return Observable.just("date error")
        }
        if (created.after(expired)) {
            return Observable.just("created > expired error")
        }
        val id = (queryParam["id"] ?: error(""))[0]!!.toInt()
        return if (mongoDriver.addTicket(Ticket(id, created, expired)) == Success.SUCCESS) {
            Observable.just("new ticket")
        } else {
            Observable.just("md error")
        }
    }

}