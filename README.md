# Franzy-Mocks

[Kafka](http://kafka.apache.org/documentation.html) producer/consumer mocks for [Franzy](https://github.com/ymilky/franzy) or vanilla Java/Clojure Kafka testing/development.

## Why

* You need to write unit tests that don't rely on an embedded instance of Kafka and/or Zookeeper.
* You are interested in testing your consumer or producer behavior, not an integration test.
* You want full control over producer/consumer records.
* You need to diagnose issues with a Kafka producer/consumer and need to isolate network and other infrastructure issues as the problem.
* You are writing a partitioner for Franzy and/or Kafka and need to test the partitioner in isolation of a real environment.

## Docs

* Read the browsable [API](http://ymilky.github.io/franzy-mocks/)
* See [Franzy Source](https://github.com/ymilky/franzy) and docs for more information about Kafka consumers/producers.
* For more information about Kafka Mocks, see the Java [Mock Producer](https://kafka.apache.org/090/javadoc/org/apache/kafka/clients/producer/MockProducer.html) and [Mock Consumer](https://kafka.apache.org/090/javadoc/org/apache/kafka/clients/consumer/MockConsumer.html)

## Installation

Add the necessary dependency to your project:

```clojure
[ymilky/franzy-mocks "0.0.1"]
```

## Usage

### Producers

The usage is much the same as a conventional Franzy producer, but a few important functions are available via protocols.

Current mock-specific operations:

* `(history p)` - Gets a history of production operations, useful for ensuring your producer is producing records how you think and in what order.
* `(clear-history! p)` - Clears the aforementioned history, useful in your unit tests if you want to continue to use the same producer.
* `(complete-next! p)` - Completes the next pending operation if you did not set operations to `auto-complete?` by default.
* `(write-exception! p e)` - Forces an exception to return as the next operations  if not using `auto-complete?` by default.

Additionally, you may also specify which partitioner is used when constructing a mock producer if you wish to test partitioning algorithms.

### Consumers

Current mock-specific operations

* `(add-record c consumer-record)` - Adds a consumer record, useful for testing pre-defined data to unit test consumption.
* `(rebalance! c topic-partitions)` - Simulates a consumer rebalance event.
* `(beginning-offsets c topic-partition-offset-map)` - Allows you to set where offsets begin when seeking to beginning of a partition, for example when using an "earliest" offset reset strategy.
* `(ending-offsets c topic-partition-offset-map)` - Allows you set where offsets end when seeking to the end of a partition, for example when using a "latest" offset reset strategy.
* `(update-partitions! c topic partitions-info)` - Allows a topic's partition info to be updated, for testing consumer responses to such changes.
* `(closed? c)` - Check to see if the producer is closed, especially useful for testing async producers with libraires such as core.async.
* `(schedule-nop-poll! c)` - Schedules a no-op poll.
* `(schedule-poll! c runnable-task)` - Schedules a Java runnable task on the next poll.
* `(write-exception! c e)` - Forces an exception to return on the next operation


## License

Copyright © 2016 Yossi M. (ymilky).

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.

Use at your own risk, I am not responsible or liable. Please give credit if you use pieces of this library or otherwise, it is much appreciated.
