package ozma.events

//FIXME handle lines length and encoding limitations inside linesIterator
//FIXME test timeouts
//FIXME delete this one keep kafka impl
class EventsStreamHandler(linesIterator: Iterator[String], eventsStats: EventsStats)
  extends EventHandler {

  def handleEvents(): Unit = {
    linesIterator.foreach(line => handleEvent(line.getBytes, eventsStats))
  }
}
