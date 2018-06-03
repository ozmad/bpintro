package ozma.bp_kafka

import java.util.Properties

import org.apache.kafka.clients.consumer.{ ConsumerConfig, ConsumerRecord, KafkaConsumer }

import scala.collection.JavaConverters._
import scala.util.Try

class BPKafkaConsumer(brokers: String, topic: String,
  groupId: String, fromBeginning: Boolean = false) {

  lazy val consumer = {
    val props = createConsumerConfig(brokers, groupId)
    val c = new KafkaConsumer[Array[Byte], Array[Byte]](props)
    c.subscribe(List(this.topic).asJava)
    c
  }

  //FIXME make sure consumer config meets requirements
  private def createConsumerConfig(brokers: String, groupId: String): Properties = {
    val props = new Properties()
    props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "100")

    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers)
    props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId)
    props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true")
    props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000")
    props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000")

    val deserializerClass = classOf[org.apache.kafka.common.serialization.ByteArrayDeserializer]
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, deserializerClass.getName)
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, deserializerClass.getName)
    props
  }

  def consume(): Iterator[ConsumerRecord[Array[Byte], Array[Byte]]] = {
    consumer.poll(1000)
      .iterator().asScala
  }

  def close(): Unit = {
    Try(consumer.close())
  }
}
