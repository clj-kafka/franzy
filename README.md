# Franzy

Franzy is a suite of Clojure libraries for [Apache Kafka](http://kafka.apache.org/documentation.html). It includes libraries for Kafka consumers, producers, partitioners, callbacks, serializers, and deserializers. 

Additionally, there are libraries for administration, testing, mocking, running embedded Kafka brokers and zookeeper clusters, and more.

The main goal of Franzy is to make life easier for working with Kafka from Clojure. Franzy provides a foundation for building higher-level abstractions for whatever your needs might be.

## Status

[![Build Status (Develop)](https://travis-ci.org/clj-kafka/franzy.svg?branch=develop)](http://travis-ci.org/clj-kafka/franzy)


## Platform

Franzy breaks up its functionality into several different libraries to minimize dependency issues. Currently, the release and the develop branch target Kafka 0.11.0.0.
All the sub-projects are organized in this monorepository, but can be used (mostly) independently.


### Core

[![Clojars Project](https://img.shields.io/clojars/v/clj-kafka.franzy/core.svg)](https://clojars.org/clj-kafka.franzy/core)

Client library - core client-oriented functionality, i.e. consumer, producer, schemas, more.


### Admin

[![Clojars Project](https://img.shields.io/clojars/v/clj-kafka.franzy/admin.svg)](https://clojars.org/clj-kafka.franzy/admin)

Administer Kafka with Clojure, get Clojure data in/out, create topics, add partitions, list brokers, etc.

### Common
[![Clojars Project](https://img.shields.io/clojars/v/clj-kafka.franzy/common.svg)](https://clojars.org/clj-kafka.franzy/common)
Common functionality for any Franzy development, and useful for Kafka in general


### Nippy de/serializer      

[![Clojars Project](https://img.shields.io/clojars/v/clj-kafka.franzy/nippy.svg)](https://clojars.org/clj-kafka.franzy/nippy)

[Nippy](https://github.com/ptaoussanis/nippy) Serializer/Deserializer for Kafka.

### Transit de/serializer

[![Clojars Project](https://img.shields.io/clojars/v/clj-kafka.franzy/transit.svg)](https://clojars.org/clj-kafka.franzy/transit)

[Transit](https://github.com/cognitect/transit-clj) Serializer/Deserializer for Kafka.


### JSON de/serializer
[![Clojars Project](https://img.shields.io/clojars/v/clj-kafka.franzy/json.svg)](https://clojars.org/clj-kafka.franzy/json)

JSON/Smile Serializer/Deserializer for Kafka using [Cheshire](https://github.com/dakrone/cheshire).

### Fressian de/serializer

[![Clojars Project](https://img.shields.io/clojars/v/clj-kafka.franzy/fressian.svg)](https://clojars.org/clj-kafka.franzy/fressian)

[Fressian](https://github.com/clojure/data.fressian) Serializer/Deserializer for Kafka.

### AVRO de/serializer

[![Clojars Project](https://img.shields.io/clojars/v/clj-kafka.franzy/avro.svg)](https://clojars.org/clj-kafka.franzy/avro)

AVRO Serializer/Deserializer for Kafka using [Abracad](https://github.com/damballa/abracad/blob/master/src/clojure/abracad/avro/edn.clj).

### Embedded broker
[![Clojars Project](https://img.shields.io/clojars/v/clj-kafka.franzy/embedded.svg)](https://clojars.org/clj-kafka.franzy/embedded)

Full featured embedded Kafka server for testing/dev, with multiple implementations including concrete types and components.

### Examples
[![Clojars Project](https://img.shields.io/clojars/v/clj-kafka.franzy/examples.svg)](https://clojars.org/clj-kafka.franzy/examples)

Growing project of examples using all the above, to learn at your leisure.

<!-- | [Franzy Mocks](https://github.com/ymilky/franzy-mocks)       | testing            | Test your consumers and producers without a running Kafka cluster, and more in the future.                                  | Franzy, Kafka client                                 | -->
<!-- | [Travel Zoo](https://github.com/ymilky/travel-zoo)           | embedded Zookeeper | Embedded Zookeeper servers and clusters for testing and development, with concrete type and component versions available.   | Curator Test                                         |-->


## Features

* Support for Kafka 0.11.0.0
* Clojure types in and out of Kafka, no worrying about the Java API or Java types
* Support for Clojure 1.8+
* A light core of external dependencies to keep things light, future-proof, and in minimal conflict with your code
* Comprehensive consumer API with support for both manual and automatic partition assignment as well as offset management
* Producer API with support for synchronous and asynchronous production
* Support for metadata and metrics
* Choice of partitioning strategies (round-robin, range) and simple helpers/framework to implement your own
* Validation for all significant data types, including configuration, via schema
* Full, validated configuration from Clojure for Consumers, Producers, Brokers, and Kafka Connect - build your config as data
* Protocols and conversions for implementing your own consumers, producers, tests, conversions, and more
<!-- * Mock producer and consumer, via [Franzy-Mocks](https://github.com/ymilky/franzy-mocks) -->
* Comprehensive Admin interface, including wrapping many undocumented/command-line only features via [![Clojars Project](https://img.shields.io/clojars/v/clj-kafka.franzy/admin.svg)](https://clojars.org/clj-kafka.franzy/admin)
* Helpers/framework for implementing custom callbacks for producers and consumers
* Helpers/framework for implementing your own serializers/deserializers
* Built-in serializers for keys and values for many data types and formats, including Strings, Integers, Longs, UUID, and Clojure Keywords, and EDN
* Add-on serializers for Nippy, JSON/JSON SMILE, and Fressian, with more to come
* A set of custom record types that fully wrap any data returned to and from Kafka, if you need, want, or prefer to use records rather than pure maps
* Ability to pass any complex parameters using provided record types which also conform to validateable schemas
* Embedded Kafka Server and components for testing via [![Clojars Project](https://img.shields.io/clojars/v/clj-kafka.franzy/embedded.svg)](https://clojars.org/clj-kafka.franzy/embedded)
* Extensive examples, code comments, and documentation
* More, coming soon....

## Why?

In addition to raw features, some reasons you may want to use Franzy:

* Comprehensive Kafka client
* Extreme care to not remove, distort, break, or diminish anything in the existing Java API
* Sane balance of performance vs. Clojure best-practices vs. ease-of-use
* Does not force any viewpoint about producing, consuming, administration, etc. on you beyond what Kafka already does
* À la carte - Lots of goodies and sugar, even for projects that are using mostly Java or don't need the consumer or producer at all. Build out what you need, no more, no less.
* Currently being used in a real project, where Kafka is the "spine" of the application, and thus, must be updated, fixed, and changed as needed
* Relatively future proof
* Designed to be a good fit with stream processors, particularly [Onyx](https://github.com/onyx-platform/onyx)
* See [Rationale](https://github.com/clj-kafka/franzy/blob/master/doc/rationale.md)

## Requirements

Requirements may vary slightly depending on your intended usage.

* Clojure 1.8+ - You may be able to compile this library on/with earlier versions, but this is untested.
* Kafka 0.11.0.0 - Some parts may work on earlier versions, but this is untested.

A good way to get started with Kafka is to use Docker and/or Vagrant. I recommend using a Docker compose stack with Kafka and Zookeeper that lets you scale up/down to test. You can also use the embedded Kafka and Zookeeper libraries listed above and discussed in the Testing/Dev section.


## Contributing/Roadmap

This library is still very young and is surely filled with bugs. Pull requests are welcome.

The following items are planned or have received partial development, I make no guarantees on timelines but plan to release some of these items in conjunction with getting other real-world work done using them:

* Pool for holding on to consumers/producers and related objects where there is less of a clear path for managing the lifetime/instantiation of an object and disposing it. Some examples - Logging, Plugins for other libraries such as Onyx, Service Calls
* Logging directly to Kafka via Timbre - dump logs directly into Kafka, presumably to process/dump them somewhere else. Useful for people with high log volumes or need some secondary processing of logs in a system like Logstash, Samza, Onyx, etc.
* Some async helpers/patterns - Many of these might just be samples, but for more generic async tools, more may be released.
* Additional tools and testing helpers, ex: parsing broker lists from franzy-admin directly to producer/consumer connnection strings.
* Even more admin tools - combining some of the existing franzy-admin operations that are naturally part of common larger operations
* Performance tweaks - some minor optimization and tweaks where possible given real-world benchmarking and usage
* Additional partitioning strategies - ex: using different hashing techniques or supporting more narrow, but common use-cases for producer partitioning

Please contact me if any of these are high-demand for you so I can guage the urgency better.

Of particular concern/value to fix/refactor/enhance currently:

* Schemas - raw, and while working, may have mistakes, which in part may be to incorrect Kafka documentation. While the Kafka source was used for some of the harder parts of this library, much of the schema came from reading the documentation. Many mistakes have already been caught. Moreover, as Kafka grows and changes, config values are often the most in flux.
* Serializers - More will be added as needed. Certainly the existing serializers can be enhanced and are meant as a blue-print and general usage. As your usage may vary, you may wish to fork or modify the existing serializers accordingly. In order to avoid this, options are easily injected into most serializers. Additional features can also be easily added by closing over values.
* Testing - more unit tests are required, but adding them is an ongoing effort.
* Field/Integration testing - If you're using this library in the wild, it may be too early, but I'd love to hear from you and fix any problems.
* Async helpers, examples, particularly with core.async and/or manifold
* Useful transformations/eductions/transductions/whatever for working with the various data structures to/from Kafka

Please be aware many problems/issues may be due to Kafka itself or the Java API. As such, before submitting issues, please check the Kafka official issue trackers first. If there is a reasonable workaround or solution, please leave a note and link to the underlying issues.

## Contact

Find me on [Clojurians Slack](https://clojurians.slack.com/) - @ymilky

... or me on twitter [@chris_betz](https://www.twitter.com/chris_betz).

## License

Copyright © 2016 Yossi M. (ymilky).

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

Use at your own risk, I am not responsible or liable. Please give credit if you use pieces of this library or otherwise, it is much appreciated.

## Acknowledgements

Thanks to the following people for advice, support, code, and/or inspiration:

* [Apache Kafka](http://kafka.apache.org) - Kafka Team, for the Java client, Kafka itself, docs, etc.
* [Lucas Bradstreet](https://github.com/lbradstreet) 
* A thank you to all authors of other Kafka clients - for inspiration and creating valuable libraries I used until my requirements changed
