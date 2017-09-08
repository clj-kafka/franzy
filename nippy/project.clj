(defproject clj-kafka.franzy/nippy "0.0.0"
  :description "A Kafka Serializer/Deserializer supporting Nippy, and an add-on for Franzy, a Clojure Kafka client."
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.apache.kafka/kafka-clients "0.9.0.1"]
                 [com.taoensso/nippy "2.11.1"]]
  :monolith/inherit true
  :middleware [leiningen.v/dependency-version-from-scm]

  :plugins
  [[lein-monolith "1.0.1"]
   [com.roomkey/lein-v "6.1.0-cb-9-0x521a"]])
