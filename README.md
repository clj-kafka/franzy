# Franzy-Embedded

[Kafka](http://kafka.apache.org/documentation.html) embedded Broker.

A good fit with [Franzy](https://github.com/ymilky/franzy), a Kafka client and suite of Kafka libraries from admin to serialization.

## What's in the Box

* Embedded Kafka 0.9+ Broker, fully supporting all configuration parameters.
* Concrete Embedded broker types for those that need or want it and prefer something that isn't a component or can be wrapped in one as desired
     * More control and easy for you to wrap in your own component if you wish
     * Full Kafka Broker lifecycle method
* [Component](https://github.com/stuartsierra/component) specific version of embedded Broker, for easy DI and integration with component systems
     * A component implementing the standard lifecycle protocol that starts/stops the Kafka broker safely (blocking)
     * A component that can compose the concrete version, if for some reason you prefer it wrapped, or need an example to develop your own
* Schemas for validating Kafka broker configurations (via [Franzy-Common](https://github.com/ymilky/franzy-common)
* More as demand necessitates

## Why

* You want to validate your Kafka Broker configurations.
* You need to do unit testing, integration testing, etc. and don't want to fire up a full Kafka cluster.
* You want to mess around with Kafka in a shell, REPL, etc.
* You want to test behaviors of consumers and producers with various broker settings and you don't want to go changing your production or even dev/testing cluster for everyone else.
* You're working in a shared environment and need something isolated to your machine(s) only.
* You tried other Kafka embedded brokers and they have some quirks/issues/etc.
     * Don't work with 0.9+
     * Slow(er)
     * No validation
     * Hide functionality and configuration parameters
* You're using the rest of [Franzy](https://github.com/ymilky/franzy) and you want a good fit.
* You want something that is used by real people.
* You want an embedded server that doesn't depend on some giant project you're including just to get at 1 namespace.

## Docs

* Read the browsable [API](http://ymilky.github.io/franzy-embedded/index.html)
* See the source for some additional comments/docs
* For more about using, validating, and developing schemas, see [Schema](https://github.com/plumatic/schema).

## Notes

* Do not use this to run a production Kafka cluster. This should be obvious and you shouldn't be using Kafka if you thought to do this. You've been warned, now we can be friends.
* You will find more schemas, types, etc. in projects with specific usages, for example producer and consumer-only schemas are in [Franzy](https://github.com/ymilky/franzy) itself.

## Installation

Add the necessary dependency to your project:

```clojure
[ymilky/franzy-embedded "0.0.1"]
```

### Requirements

* Zookeeper version supporting Kafka 0.9+, see [Kafka FAQ](https://cwiki.apache.org/confluence/display/KAFKA/FAQ)
* Clojure 1.8+ (earlier versions untested, but may work with a recompile)

Ensure you have an available instance of Zookeeper visible to the machine that will run your embedded broker. A common use-case is to also run an embedded instance of Zookeeper or to use a container such as Docker, or a VM/VM-like setup such as Vagrant.

## Usage

Ideally, you should be injecting your configuration via EDN, environment variables, params, etc.

### Typed (concrete, via deftype) Broker

First, require:

```clojure
(my.ns
  (:require [franzy.embedded.broker :as broker]
            [franzy.embedded.protocols :refer :all]))
```

If you don't have a config or want the standard localhost defaults for both Kafka and Zookeeper, you can just do:

```clojure
(let [b (broker/make-broker)]
  (startup broker)
  ;;broker is running

  ;;signal a shutdown, but may not shutdown for a bit
  (shutdown b)

  ;;stop the broker, blocks until shutdown
  (await-shutdown b)

  ;you can shortcut the above with:
  (attempt-shutdown b))
```

Alternatively, you can simply do.....

```clojure
(with-open [b (broker/make-broker)]
 ;;do terrible things with broker and its friends

 (println "The end is nigh"))
```

And if you do care about configuration, you can do:

```clojure
(with-open [b (broker/make-broker my-config)]
 ;;do terrible things with broker and its friends

 (println "In the heat of the night, I've got trouble"))
```

And if you want a thread prefix for logging or some other strange purposed

```clojure
(with-open [b (broker/make-broker config "prefix-of-prefixes")]
 ;;do terrible things with broker and its friends

 (println "This is the least interesting of the brokers in the world."))
```

### Startable Broker

If you need a startable broker, everything previous applies, just do this

```clojure
(with-open [b (broker/make-startable-broker)]
 ;;do terrible things with broker and its friends

 (println "I am startable and do slightly less safer, because I can"))
```

### Component Broker

First, require:

```clojure
(my.ns
  (:require [franzy.embedded.component :as broker]))
```

For the pure component version:

```clojure
(let [b (broker/make-embedded-broker)]
  ;;calls startup
  (component/start b)
  ;;The Kafka Server itself
  (:server b)
  ;;blocks and awaits shutdown
  (component/stop b))
```

### Composite Component Broker

First, require:

```clojure
(my.ns
  (:require [franzy.embedded.composite :as broker]))
```

For the pure component version:

```clojure
(let [b (broker/make-embedded-broker)]
  ;;calls startup on the concrete broker
  (component/start b)
  ;;The concrete broker
  (:embedded-broker b)
  ;;blocks and awaits shutdown on the concrete broker
  (component/stop b))
```

### Other Fun

Check out the protocols and tests.

A few things supported via protocols:

* Get an instance of zk-utils for the lazy, to use with admin, and even better, [Franzy-Admin](https://github.com/ymilky/franzy-admin)
* Get the bound port per channel, for example SSL or plain
* Play with server state bytes, if you know what that is and what you're doing

### Validation

Validation is performed when you attempt to create a broker. You can validate using the provided schemas in advance if you prefer.

### Config

Configuration is done just using a regular Clojure map. The broker values correspond to the official [broker configs](http://kafka.apache.org/documentation.html#brokerconfigs) to make life easier. All property keys are keywords.

You can use the config codec in [Franzy-Common](https://github.com/ymilky/franzy-common) as well if you want to convert an existing config into EDN.

Here's an example config with a few extra params to demonstrate, but you only need to provide a host name, port, broker id, and zookeeper connection string.

```clojure
{:host.name                            "127.0.0.1"
 :port                                 9092
 :broker.id                            0
 :advertised.host.name "your-insignificant-host.com"
 :advertised.port 9092
 :num.partitions                       1
 :controller.socket.timeout.ms         1500
 :controlled.shutdown.enable           true
 :replica.socket.timeout.ms            1500
 :delete.topic.enable                  true
 :controlled.shutdown.retry.backoff.ms 100
 :log.cleaner.dedupe.buffer.size       2097152
 :zookeeper.connect                    "127.0.0.1:2181"}
```

## Other

* I've had on and off problems with all embedded Kafka servers on 0.9 with the Apache Curator framework when connecting to an embedded Zookeeper instance. It could just be me, but if you're having problems, try a docker or vagrant image instead for Zookeeper.
* The schema tweaking for the broker config is always ongoing and constantly adding/removing things as Kafka itself changes. Let me know if you find anything breaking or want to make improvements.
* It's slow for me and I hate you! Try tweaking your memory settings for the JVM, especially if launching via REPL. It's not going to be fast if you're not giving it resources. With proper setup, it runs fast enough and there's nothing we're doing to cripple it.
* If you prefer to roll your own embedded server, you can use most of what's here and use what is in server.clj to create a KafkaServer to then wrap, destroy, or whatever it is you misguidedly plan to do.

## License

Copyright © 2016 Yossi M. (ymilky).

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.

Use at your own risk, I am not responsible or liable. Please give credit if you use pieces of this library or otherwise, it is much appreciated.
