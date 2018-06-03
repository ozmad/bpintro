package ozma.events

import com.typesafe.scalalogging.LazyLogging
import ozma.bp_kafka.BPKafkaConsumer

import scala.util.Try
import scala.util.control.NonFatal

class EventsKafkaHandler(kafkaBootstrap: String, kafkaTopicName: String,
  reportStatsFunc: (EventsStats) => Unit) extends EventHandler with LazyLogging {

  logger.info(s"starting events handler from kafka. kafkaBootstrap=$kafkaBootstrap, topic=$kafkaTopicName")
  var bpKafkaConsumer: BPKafkaConsumer = null
  var shouldRun = true

  def stopHandling(): Unit = {
    shouldRun = false
  }

  def getConsumer(): BPKafkaConsumer = {
    if (bpKafkaConsumer == null) {
      bpKafkaConsumer = new BPKafkaConsumer(kafkaBootstrap, kafkaTopicName,
        "eventsConsumer")
    }
    bpKafkaConsumer
  }

  private def closeKafkaConsumer(): Unit = {
    Option(bpKafkaConsumer).map(_.close())
    bpKafkaConsumer = null
  }

  def startHandling(): Unit = {
    logger.info("starting events from kafka handler")
    while (shouldRun) {
      Try {
        val kafkaConsumer = getConsumer()
        val stats = EventsStats()
        kafkaConsumer.consume().foreach { record =>
          handleEvent(record.value(), stats)
        }

        if (!stats.isEmpty()) {
          logger.debug(s"sending $stats")
          reportStatsFunc(stats)
        }

        Thread sleep 800
      } recover {
        case NonFatal(e) =>
          logger.error("error during handle from kafka", e)
          closeKafkaConsumer()
      }
    }
    closeKafkaConsumer()
    logger.info("events handler from kafka was stopped")
  }
}
