(defproject clj-kafka.franzy/admin "0.0.0"
  :description "A Kafka Clojure Admin client, with support for topic, partition, group, cluster management, and more."
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [prismatic/schema "1.1.0"]
                 [org.apache.kafka/kafka_2.11 "0.9.0.1" :exclusions [org.scala-lang/scala-library]]
                 [org.scala-lang/scala-library "2.11.8"]
                 [clj-kafka.franzy/common nil]
                 [com.taoensso/timbre "4.3.1"]]
  :monolith/inherit true
  :middleware [leiningen.v/dependency-version-from-scm]

  :plugins
  [[lein-monolith "1.0.1"]
   [chrisbetz/lein-v "6.2.0"] ;; replace with appropriate version of com.roomkey/lein-v when @cch1 accepts PR roomkey/lein-v#10 and creates new version on clojars.
   ]
  :profiles {:dev              {:dependencies [[midje "1.7.0"]
                                               [jarohen/nomad "0.7.2"]]
                                :plugins      [[lein-midje "3.2"]
                                               [lein-set-version "0.4.1"]
                                               [lein-update-dependency "0.1.2"]
                                               [lein-pprint "1.1.1"]]}
             :reflection-check {:global-vars
                                {*warn-on-reflection* true
                                 *assert*             false
                                 *unchecked-math*     :warn-on-boxed}}})
