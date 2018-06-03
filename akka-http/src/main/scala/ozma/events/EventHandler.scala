package ozma.events

import com.google.gson.JsonParser

import scala.util.Try
import scala.util.control.NonFatal

trait EventHandler {
  val jsonParser = new JsonParser()

  protected def handleEvent(eventBytes: Array[Byte], eventsStats: EventsStats): Unit = {
    Try {
      val jsonStr = new String(eventBytes)
      val eventJson = jsonParser.parse(jsonStr)
      val eventType = eventJson.getAsJsonObject.get("event_type").getAsString
      val eventData = eventJson.getAsJsonObject.get("data").getAsString
      //validate timestamp exists
      eventJson.getAsJsonObject.get("timestamp").getAsLong

      eventsStats.eventTypeCounters.inc(eventType)
      //FIXME can data field be more than one word? what is the expected structure
      //FIXME do we consider empty word as a word? if yes if 'data' is null,
      //	should we fail the event or treat as empty word?
      //	same question about empty values in 'event_type'
      eventsStats.dataWordsCounters.inc(eventData)
    }.recover {
      case NonFatal(e) =>
        //FIXME recover more specific errors
        eventsStats.eventFailedErrorCounters.inc(e.getClass.getSimpleName)
    }
  }
}
