# Franzy-Common

[Kafka](http://kafka.apache.org/documentation.html) common, useful, reusable utility library.

## What's in the Box

* Schemas for Kafka types shared by producers, consumers, brokers, admin, etc.
* Simple conversion and handling of Kafka properties files in a more Clojure way, for example keywords as property keys.
* Core Kafka data type definitions such as Topic Partitions, Nodes, Metrics, and more.
* Protocols that are reused in other [Franzy](https://github.com/ymilky/franzy) projects

## Why

* You want to validate your Kafka configurations
* You want something you can use to transform your existing Kafka configurations such as property-based configs into Clojure maps
* You want to validate frequently used types in Kafka such as topic partitions
* You are building a web service or sending types across the wire and need to validate them
* You want data types that are easy to validate, use in protocols, etc.
* You need to cut down on memory usage and increase speed in your Kafka app, so you want some records to replace where you used maps
* No dependencies on Kafka itself and no requirement to include any heavy-weight code from other libraries such as [Franzy](https://github.com/ymilky/franzy) itself
* Tested and used in an actual project - [Franzy](https://github.com/ymilky/franzy)

## Docs

* Read the browsable [API](http://ymilky.github.io/franzy-common/index.html)
* See [Franzy Source](https://github.com/ymilky/franzy) and docs for more information about schemas, types, etc.
* For more about using, validating, and developing schemas, see [Schema](https://github.com/plumatic/schema).

## Notes

* You will find more schemas, types, etc. in projects with specific usages, for example producer and consumer-only schemas are in [Franzy](https://github.com/ymilky/franzy) itself.
* This library is growing and all Franzy related code that is useful outside Franzy or across Franzy projects will be extracted into this library.

## Installation

Add the necessary dependency to your project:

```clojure
[clj-kafka.franzy/common "0.0.1"]
```

[![Clojars Project](https://img.shields.io/clojars/v/clj-kafka.franzy/common.svg)](https://clojars.org/clj-kafka.franzy/common)

## License

Copyright © 2016 Yossi M. (ymilky).

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.

Use at your own risk, I am not responsible or liable. Please give credit if you use pieces of this library or otherwise, it is much appreciated.
