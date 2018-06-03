package ozma

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.LazyLogging
import ozma.events.{ EventStatsActor, EventsKafkaHandler, EventsStats }
import ozma.routes.StatsRoutes

import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, Future }
import scala.util.Try

object BpMain extends App with LazyLogging {

  logger.info("--- starting server ---")

  implicit val system: ActorSystem = ActorSystem("AkkaHttpServer")

  implicit val executionContext = system.dispatcher

  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val eventsStats = EventsStats()

  private val statsActor = system.actorOf(EventStatsActor.props, EventStatsActor.name)

  def getEventsStats(): Future[EventsStats] = {
    EventStatsActor.getStats(statsActor)
  }

  val statsRoutes = new StatsRoutes(getEventsStats)
  lazy val routes: Route = statsRoutes.routes

  //FIXME take kafka servers and topic name from config, env or better args impl
  println("Optional arguments: kafkaServers:port kafkaTopicName (defaults are:" +
    "localhost:9092, bpevents)")

  val eventsKafkaHandler = new EventsKafkaHandler(
    Try(args(0)).getOrElse("localhost:9092"),
    Try(args(1)).getOrElse("bpevents"), (e: EventsStats) => {
      EventStatsActor.appendStats(statsActor, e)
    })

  //This Thread will stop once eventsKafkaHandler is stopped
  new Thread() {
    override def run(): Unit = eventsKafkaHandler.startHandling()
  }.start()

  Http().bindAndHandle(routes, "localhost", 8080)

  println(s"Server online at http://localhost:8080/")

  Await.result(system.whenTerminated, Duration.Inf)

  eventsKafkaHandler.stopHandling()
}
