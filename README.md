# Franzy-Examples

Contrived examples to help you learn how to use [Franzy](https://github.com/ymilky/franzy), a suite of [Kafka](http://kafka.apache.org/documentation.html) libraries.

## Why

* You have no idea what you're doing and want to use [Franzy](https://github.com/ymilky/franzy)
* Examples make you happy
* You didn't read the code, comments, documentation, or anything else I told you to
* You want to test some things in the REPL to see if your code is wrong, or it's just broken

# What's in the Box

So far, there are a few examples with lots of comments for you to follow along. Many more as time continues.

It should be self-explanatory, but if not let me know or perhaps may your deity have mercy on you.

### Producers

* Producing with strings, EDN, and [Nippy Producer](https://github.com/ymilky/franzy-nippy)
* Working with partition info and partition info metadata
* Metrics
* Producer Callbacks

### Consumers

* [Nippy Consumer](https://github.com/ymilky/franzy-nippy)
* List topics and topic metadata
* Offset Management/Metadata
* State Management (pause, resume, wakeup, etc.)
* Manual consumers
* Subscription-based consumers
* Metrics

### Admin

* Creating a topic
* Deleting a topic
* Checking if a topic exists

### Other

* Has dependencies on most other projects to help you mess around in the REPL

## Docs

* See [Franzy Source](https://github.com/ymilky/franzy) and docs for more information about serializers/deserializers.

## Installation

Clone it, friend.

1. Open configuration.clj and set things to your liking
2. Run the functions as necessary in bootstrap.clj to bootstrap your topics if needed
3. Start with the consumer examples and work your way to the producer.

A lot more examples, coming soon and as requested. If you're curious how to do something, please contact me and I'll try to post an example if it's simple and reasonable.

This repo should grow over time with more examples.

## Coming Soon

* Admin Examples
* More consumer examples
* Embedded server examples

## License

Copyright © 2016 Yossi M. (ymilky).

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.

Use at your own risk, I am not responsible or liable. Please give credit if you use pieces of this library or otherwise, it is much appreciated.
