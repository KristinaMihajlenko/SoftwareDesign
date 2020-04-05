import io.netty.buffer.ByteBuf
import io.netty.handler.codec.http.HttpResponseStatus
import io.reactivex.netty.protocol.http.server.HttpServer
import io.reactivex.netty.protocol.http.server.HttpServerRequest
import io.reactivex.netty.protocol.http.server.HttpServerResponse
import rx.Observable
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.stream.Collectors

class ReportServer(private val events: ConcurrentLinkedQueue<Event>) {
    fun run() {
        HttpServer.newServer(8083).start { req: HttpServerRequest<ByteBuf?>, resp: HttpServerResponse<ByteBuf?> ->
            val response: Observable<String>
            when (req.decodedPath.substring(1)) {
                "entry_count" -> {
                    response = Observable.just(dailyEntryCount())
                    resp.setStatus(HttpResponseStatus.OK)
                }
                "mean" -> {
                    response = Observable.just(meanVisitTime())
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

    fun dailyEntryCount(): String {
        val eventsByDay: MutableMap<String, Int> = TreeMap()
        val calendar = Calendar.getInstance()
        val format = SimpleDateFormat("dd-MM-yyyy")
        for (event in events) {
            if (event.eventType === Event.EventType.ENTER) {
                calendar.time = event.time
                val dataKey = format.format(calendar.time)
                eventsByDay[dataKey] = eventsByDay.getOrDefault(dataKey, 0) + 1
            }
        }
        if (eventsByDay.isEmpty()) {
            return "no records"
        }
        val stringBuilder = StringBuilder()
        for ((key, value) in eventsByDay) {
            stringBuilder.append(key).append(" ").append(value).append("\n")
        }
        return stringBuilder.toString()
    }

    fun meanVisitTime(): String {
        val eventsByTicketId = events.stream().collect(Collectors.groupingBy(Event::ticketId))
        if (eventsByTicketId.isEmpty()) {
            return "no records"
        }
        var sumTime: Long = 0
        var numSessions = 0
        var balance: Long = 0
        var lastEventTime: Long = 0
        for (eventList in eventsByTicketId.values) {
            for (event in eventList) {
                if (event.eventType === Event.EventType.ENTER) {
                    sumTime -= event.time.time
                    numSessions++
                    balance++
                } else {
                    sumTime += event.time.time
                    balance--
                }
                lastEventTime = lastEventTime.coerceAtLeast(event.time.time)
            }
        }
        val mean = (sumTime + balance * lastEventTime) / numSessions
        return "$mean MILLISECONDS"
    }

}