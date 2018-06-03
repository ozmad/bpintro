package ozma.utils

import ozma.exceptions.{ BPErrorCode, BPException }

//Use carefully counted entries are never removed, do not use too unique counterId
//TODO make counts private or find better way to json convert for rest API
case class Counters() {

  var counts = Map[String, Long]()

  def appendFrom(other: Counters): Unit = {
    other.counts.foreach { case (k, v) => inc(k, v) }
  }

  def get(counterId: String): Option[Long] = counts.get(counterId)

  //returns the updated value for counterId
  def inc(counterId: String, counted: Long = 1): Long = synchronized {
    if (counted < 0) {
      throw new BPException(s"Got negative count for $counterId", BPErrorCode.InvalidValue)
    }

    val newCount = counted + counts.getOrElse(counterId, 0L)
    counts = counts ++ Map(counterId -> newCount)

    newCount
  }

  def isEmpty(): Boolean = counts.isEmpty

  override def toString: String = counts.toString()
}
