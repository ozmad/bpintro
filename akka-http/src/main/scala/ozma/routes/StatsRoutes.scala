package ozma.routes

import akka.http.scaladsl.model.{ ContentTypes, HttpEntity }
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import com.google.gson.Gson
import ozma.events.EventsStats

import scala.collection.JavaConverters._
import scala.concurrent.{ ExecutionContext, Future }

class StatsRoutes(getEventsStatsFunc: () => Future[EventsStats])(implicit executionContext: ExecutionContext) {

  case class EventsStatsRes(
    types: java.util.Map[String, Long],
    words: java.util.Map[String, Long])

  val gson = new Gson()

  def eventsStatsToRes(eventsStats: EventsStats): EventsStatsRes = {
    EventsStatsRes(
      eventsStats.eventTypeCounters.counts.asJava,
      eventsStats.dataWordsCounters.counts.asJava)
  }

  lazy val routes = pathPrefix("stats") {
    get {
      val statsJsonStrF = getEventsStatsFunc()
        .map { eventsStats => gson.toJson(eventsStatsToRes(eventsStats)) }

      //TODO handle errors better
      onSuccess(statsJsonStrF) { statsJsonStr =>
        val entity = HttpEntity(ContentTypes.`application/json`, statsJsonStr)
        complete(entity)
      }
    }
  }
}
