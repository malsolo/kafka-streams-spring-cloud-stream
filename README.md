# Spring Cloud Stream - Apache Kafka Streams Binder

## Introduction
See:

* [Introducing Spring Cloud Stream](https://docs.spring.io/spring-cloud-stream/docs/3.1.2/reference/html/spring-cloud-stream.html#spring-cloud-stream-overview-introducing)
* [Main concepts](https://docs.spring.io/spring-cloud-stream/docs/3.1.2/reference/html/spring-cloud-stream.html#_main_concepts)

## Spring Cloud Stream Kafka Streams Binder Reference Guide
See:

* [Kafka Streams Binder](https://docs.spring.io/spring-cloud-stream-binder-kafka/docs/3.1.2/reference/html/spring-cloud-stream-binder-kafka.html#_kafka_streams_binder)

### Usage

In a Spring boot application, add the dependencies:
* Cloud Stream (spring-cloud-stream-binder-kafka-streams)
* Spring for Apache Kafka Streams (kafka-streams)

### Programming Model - Functional style

We recommend using the functional programming model. The support for StreamListener is deprecated starting with 3.1.0 of Spring Cloud Stream.

Example:
```
@SpringBootApplication
public class WordCountProcessorApplication {

  @Bean
  public Function<KStream<Object, String>, KStream<?, WordCount>> process() {

    return input -> input
                .flatMapValues(value -> Arrays.asList(value.toLowerCase().split("\\W+")))
                .map((key, value) -> new KeyValue<>(value, value))
                .groupByKey(Serialized.with(Serdes.String(), Serdes.String()))
                .windowedBy(TimeWindows.of(5000))
                .count(Materialized.as("word-counts-state-store"))
                .toStream()
                .map((key, value) -> new KeyValue<>(key.key(), new WordCount(key.key(), value,
                        new Date(key.window().start()), new Date(key.window().end()))));
  }

  public static void main(String[] args) {
    SpringApplication.run(WordCountProcessorApplication.class, args);
  }
}
```

With this configuration, the application will consume from a topic and produce to another topic. If you just want to
consume from a topic, create a bean of type *java.util.function.Consumer<...>* instead.

By default, the binder will configure the input and output topic using the function name: process-in-0 and process-out-0 respectively.

For customizing these values use the application properties:
* spring.cloud.stream.bindings.process-in-0.destination=input_topic_name
* spring.cloud.stream.bindings.process-out-0.destination=output_topic_name

**Multiple inputs**: for two inputs you can use a **BiFunction**. Beyond two inputs, you should use **curried functions**.
We've found this approach too complicated, so we decided to create separate beans that end up in several Kafka Streams applications, i.e.,
the binder will create several separate Kafka Streams objects with different application ID’s.
That is: [Multiple Kafka Streams processors within a single application](https://docs.spring.io/spring-cloud-stream-binder-kafka/docs/3.1.2/reference/html/spring-cloud-stream-binder-kafka.html#_multiple_kafka_streams_processors_within_a_single_application)

If you have more than one processor in the application, you have to tell Spring Cloud Stream, which functions need to be activated:
* spring.cloud.stream.function.definition: processOne;processTwo;processN

**Kafka Streams Application ID**. Application id is a mandatory property that you need to provide for a Kafka Streams application,
by default, binder will auto generate the application ID per function. For customizing it, since have multiple processors, 
we use this property to set the application ID:
* spring.cloud.stream.kafka.streams.binder.functions.<function-name>.applicationId: name

### Kafka configuration

Spring boot allows you to configure Kafka with _spring.kafka.*_ properties, for instance, **spring.kafka.bootstrapServers**.

You can configure it with binder properties, that Kafka Streams Binder will check first: **spring.cloud.stream.kafka.streams.binder.brokers**.

### Configuration

See [Kafka Streams Binder Properties](https://docs.spring.io/spring-cloud-stream-binder-kafka/docs/3.1.2/reference/html/spring-cloud-stream-binder-kafka.html#_kafka_streams_binder_properties)

For adding additional Kafka Streams API (the one from Kafka, not the Spring Cloud Streams one) add key-value properties 
using the prefix **spring.cloud.stream.kafka.streams.binder.configuration**. For instance, to configure the Schema Registry,
default Serdes, and so on:

```

```

### Serialization and deserialization.

See [Record serialization and deserialization](https://docs.spring.io/spring-cloud-stream-binder-kafka/docs/3.1.2/reference/html/spring-cloud-stream-binder-kafka.html#_record_serialization_and_deserialization)

BLA

### Error Handling

See [Error Handling](https://docs.spring.io/spring-cloud-stream-binder-kafka/docs/3.1.2/reference/html/spring-cloud-stream-binder-kafka.html#_error_handling)

BLA

### State Store

See [State Store](https://docs.spring.io/spring-cloud-stream-binder-kafka/docs/3.1.2/reference/html/spring-cloud-stream-binder-kafka.html#_state_store)

BLA

### Interactive Queries

See [Interactive Queries](https://docs.spring.io/spring-cloud-stream-binder-kafka/docs/3.1.2/reference/html/spring-cloud-stream-binder-kafka.html#_interactive_queries)

BLA

# Test the application

## Start kafka

Start
```
$ docker-compose up -d
```

Verify

```
$ docker-compose ps
```

Check

```
$ docker-compose logs broker -f
```

## Topics management

```
$ docker exec -it broker kafka-topics --list --bootstrap-server localhost:9092

$ docker exec -it broker kafka-topics --delete --bootstrap-server localhost:9092 --topic kafka-streams-demo-transactions
$ docker exec -it broker kafka-topics --delete --bootstrap-server localhost:9092 --topic kafka-streams-demo-purchases
$ docker exec -it broker kafka-topics --delete --bootstrap-server localhost:9092 --topic kafka-streams-demo-purchases-keyed
$ docker exec -it broker kafka-topics --delete --bootstrap-server localhost:9092 --topic kafka-streams-demo-patterns
$ docker exec -it broker kafka-topics --delete --bootstrap-server localhost:9092 --topic kafka-streams-demo-rewards
$ docker exec -it broker kafka-topics --delete --bootstrap-server localhost:9092 --topic kafka-streams-demo-correlated-purchases
$ docker exec -it broker kafka-topics --delete --bootstrap-server localhost:9092 --topic kafka-streams-demo-customer-transactions

$ docker exec -it broker kafka-topics --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic kafka-streams-demo-transactions
$ docker exec -it broker kafka-topics --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic kafka-streams-demo-purchases
$ docker exec -it broker kafka-topics --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic kafka-streams-demo-purchases-keyed
$ docker exec -it broker kafka-topics --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 2 --topic kafka-streams-demo-patterns
$ docker exec -it broker kafka-topics --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 3 --topic kafka-streams-demo-rewards
$ docker exec -it broker kafka-topics --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic kafka-streams-demo-correlated-purchases
$ docker exec -it broker kafka-topics --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic kafka-streams-demo-customer-transactions

$ docker exec -it broker kafka-topics --list --bootstrap-server localhost:9092 | grep kafka-streams-demo-

```

## Run the application

## Produce and consume messages

### Produce messages

### Consume messages

```
$ bin/kafka-avro-console-consumer --bootstrap-server localhost:9092 --topic kafka-streams-demo-transactions --property schema.registry.url=http://localhost:8081 --from-beginning | jq .

$ bin/kafka-avro-console-consumer --bootstrap-server localhost:9092 --topic kafka-streams-demo-purchases --property schema.registry.url=http://localhost:8081 --from-beginning | jq .
```
