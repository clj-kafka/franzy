(defproject clj-kafka.franzy/core "0.0.0"
  :description "Clojure Kafka client with support for Kafka producer, consumer, rebalancing, administration, and validation."
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [prismatic/schema "1.0.5"]
                 [org.apache.kafka/kafka-clients "0.9.0.1"]
                 [lbradstreet/franzy-common "0.0.2"]]
  :monolith/inherit true
  :middleware [leiningen.v/dependency-version-from-scm]

  :plugins
  [[lein-monolith "1.0.1"]
   [chrisbetz/lein-v "6.2.0"] ;; replace with appropriate version of com.roomkey/lein-v when @cch1 accepts PR roomkey/lein-v#10 and creates new version on clojars.
   ]
  :profiles {:dev {:dependencies [[com.taoensso/timbre "4.3.1"]]}})
