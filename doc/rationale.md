# Rationale

* During development I find I spent too much time in shell sessions talking to Kafka. This is tedious to say the least.
* I had work situations where I couldn't get shell access on the machine.
* I tried many Kafka admin packages, including web interfaces, and they did one or more of the following:
     * Full of terrible bugs, slow, and/or returned incorrect results
     * Missing huge chunks of what is possible
     * Completely out-of-date with Kafka 0.9+
     * Screwed up Zookeeper in particular due to incorrect understandings of how/where/why things are stored there
     * Required lots of huge Scala/Java objects just to make the simplest of queries
     * Leaked memory and/or resources
     * Required that they run directly on the Zookeeper or Kafka server
* Kafka is changing all the time, it's best to use the official tools if you're on the JVM
* The Scala API is arguably a mess. I believe it's hard to argue as there are some Jira work items regarding fixing it.
* Mucking around with Zookeeper gives a lot of power, but makes it easy to screw things up badly, but still wanted to at least expose a bit to get started for the brave
* I needed to dynamically do a lot of actions in Kafka ranging from creating/deleting topics to getting information about partitioning changes, not just via consumer/producer which don't do most of that
* I wanted nothing to do with writing shell scripts against Kafka. Writing Clojure is cleaner and more maintainable, thank you. I say this is as a long-time unix, and later linux user.
     * Shell scripts are often full of corner cases because most developers on teams are not as good at shell programming as they think
     * I find Clojure much more expressive than anything offered by the combinations of bash, sed, shell, regex, awk, etc.
     * Error handling and threading in the shell is hard to get right
     * Dynamically creating complex shell calls is annoying and hard to maintain
     * The shell introduces an extra layer of potential issues, conversions, and overhead translating arguments and options to Java, only to call some of the exact same APIs
     * Sometimes the shell commands don't support the full range of possibilities because someone hasn't had time, forgot, screwed up, etc.

* I wanted to collect data from Kafka and send it over the wire
* I wanted to take parts of this data and throw it into Riemann, Logstash, etc.
* I wanted to put the results I collected from Kafka onto core.async channels easily and fast
* I needed to make dynamic decisions about my live consumers and producers
* Writing Clojure apps and working with Scala data structures sucks. No thanks.
* I pretty much wanted to do everything listed in "Why"
* More, but it makes me too angry to think about it

There are probably some great clients out there, but they all had problems in one way or another. Though the official tools that this library wraps generally work and there's no reason you can't use them directly, it's a pain and not nice when working in Clojure.

Hence, I gve you Franzy-Admin.
