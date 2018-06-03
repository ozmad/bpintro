package ozma.events

import ozma.utils.Counters

case class EventsStats(
  eventFailedErrorCounters: Counters = Counters(),
  eventTypeCounters: Counters = Counters(),
  dataWordsCounters: Counters = Counters()) {

  def appendFrom(other: EventsStats): Unit = {
    eventTypeCounters.appendFrom(other.eventTypeCounters)
    dataWordsCounters.appendFrom(other.dataWordsCounters)
    eventFailedErrorCounters.appendFrom(other.eventFailedErrorCounters)
  }

  def isEmpty(): Boolean = {
    eventFailedErrorCounters.isEmpty() &&
      eventTypeCounters.isEmpty() &&
      dataWordsCounters.isEmpty()
  }
}
