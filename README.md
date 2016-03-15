# Franzy-Admin

Franzy Admin is a comprehensive set of administration tools for Administering [Kafka](http://kafka.apache.org/).

It fits in well with [Franzy](https://github.com/ymilky/franzy), but there's no dependency. Only what you need to administer Kafka is included, there's no dependency on the consumer/producer client itself.

## What's in the Box

* API for querying Kafka clusters that includes operations on topics, partitions, consumer groups, brokers, and more.
* A wrapper on the Zookeeper objects Kafka itself uses, and a few Kafka-specific Zookeeper helpers.
* [Schemas](https://github.com/plumatic/schema) for validating everything that is sent to/returned from Kafka.
* A full set of conversions from the Kafka Scala/Java APIs to Clojure - only Clojure in/out, including a conversion protocol.
* A set of protocols for you to use, should you choose to implement an alternative/testing version.
* Set of examples via test of what you can do.
* Comprehensive set of record types for you to use if you want something more explicit to/from Kafka, or need the performance/memory gains that are potentially possible.
* Full validation and support for configurations involved with administering Kafka such as Topic and Broker configs. Write your config as Clojure/EDN.

## Why

* You want to interact with Kafka, administer it, and work with Clojure in/out only, and do things like transduce the results.
* You want to administer your Kafka cluster programatically, possibly from a REPL, build-script, lein plugin, unit test, etc.
* You have some tasks like creating topics, adding partitions, etc. that you want to do against Kafka that can't be done via a consumer or producer easilyl
* You need to get metadata about Kafka topics, partitions, consumer groups, etc. to make your Kafka client work correctlyl
* Your application needs to make decisions in Kafka-related code such as a producer, consumer, ETL job, stream processing job based on live data from your Kafka clusterl
* You hate the command-line tools with a passion, don't want to learn all their syntax/options, hate typing, want richer error handling from Clojure, etc.
* You don't have shell access to your Kafka cluster, but you can connect to it via the usual ports for Kafka and Zookeeper.
* You want to gather some information about Kafka for something like Logstash, Riemann, JMX, etc.
* You want to modify configurations for topics, clients, etc.
* Your Kafka cluster is totally screwed up and you need to fix it, possibly via Zookeeper.
* You want to build a web-service for administering/scripting/controlling Kafka and doing virtually no work to serialize data in/out of Kafka as Clojure via JSON, Transit, EDN, etc. would make your life easy.
* You don't want to install some random web application just to work with your Kafka dev, or even production environment. Or maybe you don't want to give out that kind of access.
* You want an admin interface that is pure-data.
* You want an admin interface that implements the admin functions correctly or through hacks like launching shell calls on the server - we use what Kafka itself uses.
* The official API is being improved, but in the meantime it is dizzying and full of giant whale classes that have inconsistent parameters and all kinds of crazy stuff that goes away using this library - a more organized approach.
* You want docs - the official APIs have almost none.

## Docs

* Read the browsable [API](http://ymilky.github.io/franzy-admin/api/index.html)
* See [Franzy Source](https://github.com/ymilky/franzy) and docs for more information about schemas, types, etc.
* For more about using, validating, and developing schemas, see [Schema](https://github.com/plumatic/schema).
* Commented source and tests

## Installation

Add the necessary dependency to your project:

```clojure
[ymilky/franzy-admin "0.0.1"]
```

## Requirements

* Running instance of Kafka 0.9+ and Zookeeper
* Clojure 1.8+ (earlier versions untested but may work with dep change)

## Usage

Most operations require a connection to your Kafka cluster's Zookeeper. You should have permission and network accessibility to Zookeeper to use most of the API provided.

**More Examples Coming Soon, also view some of the tests for more**

### Client Connection

First require the necessary namespaces to create a client:

```clojure
(my.ns
  (:require [franzy.admin.zookeeper.client :as client]))
```

There are a few ways to create a client and many options (see docs/source).

You can create a connection via a Clojure map or Java Properties, but prefer a Clojure map for making data-based configs.

The simplest:

```clojure
;;create a client with only the defaults and a non-secure connection
(client/make-zk-utils {:servers "127.0.0.1:2181"} false))

;;a more custom example, using a secure connection to Zookeeper and multiple servers
(let [config   {:servers ["aye-aye.caged-animals.com" "angry-emu.caged-animals.com"]
                :connection-timeout 30000
                :session-timeout 30000                                   ;;from ZkConnection default
                :operation-retry-timeout (long -1)                       ;;from ZkClient default
                :serializer (serializers/zk-string-serializer)}]         ;;from the divine

(client/make-zk-utils config true))
```

Keep in mind the connection to Zookeeper must be closed, so wrap it with a `with-open`, try/catch/finally, or otherwise explicitly close it.

Now that you've been warned, time to actually do some useful things. Subsequent examples assume you have stored your client in a binding `zk-utils`.

### Topics

This section is a brief overview of some sample operations:

Get a list of topics:

```clojure
(all-topics zk-utils)
```

Create a topic....optionally you can specify a replication factor and topic configuration as well:

```clojure
(let [topic-name "customer-data-to-sell-to-advertisers"
      partition-count 42 ;;obviously]
(create-topic! topic-name partition-count))

(let [topic-name "customer-data-to-sell-to-advertisers"
      partition-count 42 ;;obviously
      replication-factor 3 ;;tri-force of replicas
      topic-config         {;;cover tracks
                           :cleanup.policy      :delete
                           ;;24 hours to abuse user trust
                           :delete.retention.ms 86400000
                           ;;fsync after every 10 messages, living dangerously
                           :flush.messages      10
                           ;;fsync on some nerdy interval
                           :flush.ms            32768}]
(create-topic! topic-name partition-count replication-factor topic-config))

```

Currently supported operations:

* Create topics
* List all topics
* List topics by consumer group
* List topic metadata such as partitions, leaders, replicas, ISRs, errors, etc.
* Delete topics
* Query topic replica assignments
* Zookeeper path helpers for querying and deleting topics, if you need custom Zookeeper logic

### Partitions

* List all partitions for a topic
* List all topic partitions in the cluster
* Add partitions to a topic
* List the replica assignments for a topic partition
* List all in-sync replicas for a topic partition
* Query leader election status/leader id for a topic partition
* List all the partition leaders per topic, and partitions within each topic
* List all partition metadata for a given set of topic partitions
* List partition metadata for a single topic partition
* List partitions undergoing reassignment
* List partitions undergoing replica election
* Query the partition epoch for a given topic partition
* Update partition assignment configuration
* Zookeeper path helpers for querying and deleting topics, if you need custom Zookeeper logic

'''
Note: You may not decrease the number of partitions as this currently breaks Kafka semantics. Instead, delete the topic.
'''

### Consumer Groups

* List all consumer groups
* List all consumer groups for a topic
* List all topics a consumer group is consuming from
* Check if a consumer group is active
* List the consumers in the group
* Delete a consumer group
* Delete all consumer groups for a topic

### Cluster

Currently supported operations:

* List all brokers along with their metadata such as endpoints
* List all broker ids, if all you need is IDs for other operations
* List metadata for a specific broker id.
* List all endpoints for a particular channel, ex: SSL, Plaintext, etc.
* Manual and automatic replica assignments to brokers
* Zookeeper path helpers for querying and deleting cluster-related items, if you need custom Zookeeper logic

### Entity Configuration

Currently supported operations:

* List all configurations
* List all topic or client configurations
* List all topic configurations only
* List all client configurations only
* List the names of all entities (clients/topics) with configurations
* Query for a specific configuration, for example a specific topic
* Filter out default configurations
* Update topic configurations
* Update client configurations
* Query Znode data for configurations from Zookeeper
* Zookeeper path helpers for querying and deleting cluster-related items, if you need custom Zookeeper logic

### Other

* Plenty more functionality, and not limited to the previous feature lists. Try it!
* More examples soon! Check the tests too!

## Road Map/Notes

* If something is missing here, please ask
* In the future, additional functionality from other libraries and more custom, common admin tasks may be included
* Many improvements to be made, for example schema, more detailed encoding/decoding, etc. It's fine for my purposes, but I'm happy to accommodate pull requests.
* More field testing

## Disclaimer

I have not tried this much with a production Kafka instance. This library is young and needs more testing and some improvements. This disclaimer will probably change as this library undergoes more community testing.

Please try it in development first, with the understanding that admin functions can easily wreck your Kafka setup.

Although we try to stick to the official APIs, they too may have bugs. It's better to be safe than sorry. Test first!

You have been warned. If you screw things up, it is likely something one of us did.

## License

Copyright © 2016 Yossi M. (ymilky).

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.

Use at your own risk, I am not responsible or liable. Please give credit if you use pieces of this library or otherwise, it is much appreciated.
