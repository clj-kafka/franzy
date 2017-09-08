(defproject clj-kafka.franzy/embedded "0.0.0"
  :description "Embedded Kafka servers with full configuration and validation, components, and more."
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [prismatic/schema "1.1.0"]
                 [com.taoensso/timbre "4.3.1"]
                 [com.stuartsierra/component "0.3.1"]
                 [org.apache.kafka/kafka_2.11 "0.9.0.1" :exclusions [org.scala-lang/scala-library]]
                 [org.scala-lang/scala-library "2.11.8"]
                 [clj-kafka.franzy/common nil]]
  :monolith/inherit true
  :middleware [leiningen.v/dependency-version-from-scm]

  :plugins
  [[lein-monolith "1.0.1"]
   [com.roomkey/lein-v "6.1.0-cb-9-0x521a"]]
  :profiles {:dev {:dependencies [[jarohen/nomad "0.7.2"]]}})
