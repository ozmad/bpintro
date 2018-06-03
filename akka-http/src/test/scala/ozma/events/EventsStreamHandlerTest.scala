package ozma.events

import com.google.gson.{ Gson, JsonParser }
import org.scalatest.{ Matchers, WordSpec }

case class Event(event_type: String, data: String, timestamp: Long)

//FIXME have similar test for kafka events consumer
class EventsStreamHandlerTest extends WordSpec with Matchers {
  val gson = new Gson()

  "parse event" should {
    "succeed" in {
      val json = """{ "event_type": "a", "data": "b", "timestamp": 1}"""
      val eventJson = new JsonParser().parse(json)
      eventJson.getAsJsonObject.get("event_type").getAsString shouldBe "a"
      eventJson.getAsJsonObject.get("data").getAsString shouldBe "b"
      eventJson.getAsJsonObject.get("timestamp").getAsLong shouldBe 1L
    }

    "get null if timestamp is missing" in {
      val json = """{ "event_type": "a", "data": "b"}"""
      val eventJson = new JsonParser().parse(json)
      eventJson.getAsJsonObject.get("event_type").getAsString shouldBe "a"
      eventJson.getAsJsonObject.get("data").getAsString shouldBe "b"
      eventJson.getAsJsonObject.get("timestamp") shouldBe null
    }
  }

  "EventsStats" should {
    "count parse failure" in {
      val badJson = "a"
      val linesIterator = List(badJson).iterator
      val eventsStats = EventsStats()
      val eventsStreamHandler = new EventsStreamHandler(linesIterator, eventsStats)

      eventsStreamHandler.handleEvents()

      eventsStats.eventFailedErrorCounters.get("IllegalStateException") shouldBe Option(1L)
    }

    "count failure missing field" in {
      val badJson = "{}"
      val linesIterator = List(badJson).iterator
      val eventsStats = EventsStats()
      val eventsStreamHandler = new EventsStreamHandler(linesIterator, eventsStats)

      eventsStreamHandler.handleEvents()

      eventsStats.eventFailedErrorCounters.get("NullPointerException") shouldBe Option(1L)
    }

    "count when no events" in {
      val linesIterator = List.empty.iterator
      val eventsStats = EventsStats()
      val eventsStreamHandler = new EventsStreamHandler(linesIterator, eventsStats)

      eventsStreamHandler.handleEvents()

      eventsStats.isEmpty() shouldBe true
    }

    "count events and words" in {
      val event1json = gson.toJson(Event("typeX", "w1", 1))
      val event2json = gson.toJson(Event("typeY", "w1", 1))
      val event3json = gson.toJson(Event("typeX", "w2", 1))

      val linesIterator = List(event1json, event2json, event3json).iterator
      val eventsStats = EventsStats()
      val eventsStreamHandler = new EventsStreamHandler(linesIterator, eventsStats)

      eventsStreamHandler.handleEvents()

      eventsStats.eventFailedErrorCounters.isEmpty() shouldBe true
      eventsStats.eventTypeCounters.get("typeX") shouldBe Option(2L)
      eventsStats.eventTypeCounters.get("typeY") shouldBe Option(1L)
      eventsStats.dataWordsCounters.get("w1") shouldBe Option(2L)
      eventsStats.dataWordsCounters.get("w2") shouldBe Option(1L)
    }

    "count events in small sample file" in {
      val linesIterator = scala.io.Source.fromFile("src/test/resources/simple-sample")
        .getLines()

      val eventsStats = EventsStats()
      val eventsStreamHandler = new EventsStreamHandler(linesIterator, eventsStats)

      eventsStreamHandler.handleEvents()

      eventsStats.eventFailedErrorCounters.counts.size shouldBe 2
      eventsStats.eventFailedErrorCounters.get("IllegalStateException") shouldBe Option(2)
      eventsStats.eventFailedErrorCounters.get("NullPointerException") shouldBe Option(2L)
      eventsStats.eventTypeCounters.get("bar") shouldBe Option(1L)
      eventsStats.eventTypeCounters.get("baz") shouldBe Option(2L)
      eventsStats.dataWordsCounters.get("lorem") shouldBe Option(2L)
      eventsStats.dataWordsCounters.get("dolor") shouldBe Option(1L)
    }
  }
}
