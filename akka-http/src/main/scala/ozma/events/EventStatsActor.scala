package ozma.events

import akka.actor._
import akka.event.Logging
import akka.pattern.AskableActorRef
import akka.util.Timeout

import scala.concurrent.Future
import scala.concurrent.duration._

private case object GetStats

object EventStatsActor {
  val props: Props = Props[EventStatsActor]
  val name = "EventStatsActor"

  def getStats(askableActorRef: AskableActorRef): Future[EventsStats] = {
    implicit val timeout = Timeout(5.seconds)
    (askableActorRef ? GetStats).mapTo[EventsStats]
  }

  def appendStats(actorRef: ActorRef, eventsStats: EventsStats): Unit = {
    actorRef ! eventsStats
  }
}

class EventStatsActor extends Actor {

  lazy val logger = Logging(this.context.system, this.getClass)

  val eventsStatsAccumulate = EventsStats()

  override def receive: Receive = {
    case eStats: EventsStats =>
      logger.info(s"actor got stats $eStats")
      eventsStatsAccumulate.appendFrom(eStats)
    case GetStats =>
      logger.info(s"actor sending $eventsStatsAccumulate")
      sender() ! eventsStatsAccumulate
  }
}
