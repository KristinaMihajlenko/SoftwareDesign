import com.mongodb.rx.client.Success
import io.netty.buffer.ByteBuf
import io.netty.handler.codec.http.HttpResponseStatus
import io.reactivex.netty.protocol.http.server.HttpServer
import io.reactivex.netty.protocol.http.server.HttpServerRequest
import io.reactivex.netty.protocol.http.server.HttpServerResponse
import rx.Observable
import java.util.*

class TurnstileServer(private val mongoDriver: MongoDriver) {
    fun run() {
        HttpServer.newServer(8082).start { req: HttpServerRequest<ByteBuf?>, resp: HttpServerResponse<ByteBuf?> ->
            val response: Observable<String>
            val action = req.decodedPath.substring(1)
            val queryParam = req.queryParameters
            when (action) {
                "enter" -> {
                    response = enter(queryParam, Date())
                    resp.setStatus(HttpResponseStatus.OK)
                }
                "exit" -> {
                    response = exit(queryParam, Date())
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

    fun enter(queryParam: Map<String?, List<String?>?>, date: Date): Observable<String> {
        val id = queryParam["id"]!![0]!!.toInt()
        val ticket = mongoDriver.getLatestTicketVersion(id) ?: return Observable.just("no tickers")
        if (date.after(ticket.expirationDate)) {
            return Observable.just("expired")
        }
        val event = Event(id, date, Event.EventType.ENTER)
        return if (mongoDriver.addEvent(event) == Success.SUCCESS) {
            Observable.just("enter")
        } else {
            Observable.just("error")
        }
    }

    fun exit(queryParam: Map<String?, List<String?>?>, date: Date?): Observable<String> {
        val id = queryParam["id"]!![0]!!.toInt()
        val event = Event(id, date!!, Event.EventType.EXIT)
        return if (mongoDriver.addEvent(event) == Success.SUCCESS) {
            Observable.just("exit")
        } else {
            Observable.just("error")
        }
    }

}