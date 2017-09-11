(defproject clj-kafka.franzy/common "0.0.0"
  :description "Common resources for Kafka libraries, such as schemas, utility functions, and configuration."
  :monolith/inherit true

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [prismatic/schema "1.1.0"]]
  :plugins [[lein-monolith "1.0.1"]
            [com.roomkey/lein-v "6.2.0"]]
  :profiles {:dev {:dependencies [[jarohen/nomad "0.7.2"]]}})
