package cakesolutions.kafka.akka

import cakesolutions.kafka.KafkaProducerRecord
import org.apache.kafka.clients.producer.ProducerRecord

import scala.reflect.runtime.universe.{TypeTag, typeTag}

/**
  * Helper functions for [[ProducerRecords]].
  */
object ProducerRecords {

  /**
    * Create producer records from a sequence of values.
    * All the records will have the given topic and no key.
    *
    * @param topic topic to write the records to
    * @param values values of the records
    * @param response optional response message to the sender
    */
  def fromValues[Value: TypeTag](
    topic: String,
    values: Seq[Value],
    response: Option[Any]): ProducerRecords[Nothing, Value] = {

    val records = values.map(value => KafkaProducerRecord(topic, value))
    ProducerRecords[Nothing, Value](records, response)
  }

  /**
    * Create producer records from a single key and multiple values.
    * All the records will have the given topic and key.
    *
    * @param topic topic to write the records to
    * @param key key of the records
    * @param values values of the records
    * @param response optional response message to the sender
    */
  def fromValuesWithKey[Key: TypeTag, Value: TypeTag](
    topic: String,
    key: Key,
    values: Seq[Value],
    response: Option[Any]): ProducerRecords[Key, Value] = {

    val records = values.map(value => KafkaProducerRecord(topic, key, value))
    ProducerRecords(records, response)
  }

  /**
    * Create producer records from topics and values.
    * All the records will have no key.
    *
    * @param valuesWithTopic a sequence of topic and value pairs
    * @param response optional response message to the sender
    */
  def fromValuesWithTopic[Value: TypeTag](
    valuesWithTopic: Seq[(String, Value)],
    response: Option[Any]): ProducerRecords[Nothing, Value] = {

    val records = valuesWithTopic.map {
      case (topic, value) => KafkaProducerRecord(topic, value)
    }
    ProducerRecords[Nothing, Value](records, response)
  }

  /**
    * Create producer records from key-value pairs.
    * All the records will have the given topic.
    *
    * @param topic topic to write the records to
    * @param keyValues a sequence of key and value pairs
    * @param response optional response message to the sender
    */
  def fromKeyValues[Key: TypeTag, Value: TypeTag](
    topic: String,
    keyValues: Seq[(Key, Value)],
    response: Option[Any]): ProducerRecords[Key, Value] = {

    val records = keyValues.map {
      case (key, value) => KafkaProducerRecord(topic, key, value)
    }
    ProducerRecords(records, response)
  }

  /**
    * Create producer records from topic, key, and value triples.
    *
    * @param keyValuesWithTopic a sequence of topic, key, and value triples.
    * @param response optional response message to the sender
    */
  def fromKeyValuesWithTopic[Key: TypeTag, Value: TypeTag](
    keyValuesWithTopic: Seq[(String, Key, Value)],
    response: Option[Any]): ProducerRecords[Key, Value] = {

    val records = keyValuesWithTopic.map {
      case (topic, key, value) => KafkaProducerRecord(topic, key, value)
    }
    ProducerRecords(records, response)
  }

  /**
    * Convert consumer records to key-value pairs.
    * All the records will have the given topic, and the offsets are used as the response message.
    *
    * @param topic topic to write the records to
    * @param consumerRecords records from [[KafkaConsumerActor]]
    */
  def fromConsumerRecords[Key: TypeTag, Value: TypeTag](
    topic: String,
    consumerRecords: ConsumerRecords[Key, Value]) = {

    ProducerRecords(
      consumerRecords.toProducerRecords(topic),
      Some(consumerRecords.offsets)
    )
  }

  /**
    * Create an extractor for pattern matching any value with a specific [[ProducerRecords]] type.
    *
    * @tparam Key the type of the key to match in the extractor
    * @tparam Value the type of the value to match in the extractor
    * @return an extractor for given key and value types
    */
  def extractor[Key: TypeTag, Value: TypeTag]: Extractor[Any, ProducerRecords[Key, Value]] =
    new TypeTaggedExtractor[ProducerRecords[Key, Value]]
}

/**
  * Records that [[KafkaProducerActor]] can write to Kafka.
  *
  * @param records the records that are to be written to Kafka
  * @param response an optional message that is to be sent back to the sender after records have been written to Kafka
  */
final case class ProducerRecords[Key: TypeTag, Value: TypeTag](
  records: Iterable[ProducerRecord[Key, Value]],
  response: Option[Any] = None)
  extends TypeTagged[ProducerRecords[Key, Value]]
