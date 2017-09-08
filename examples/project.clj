(defproject clj-kafka.franzy/examples "0.0.0"
  :description "Examples for Franzy"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/core.async "0.2.374"]
                 [com.taoensso/timbre "4.3.1"]
                 [ymilky/travel-zoo "0.0.2"]
                 [jarohen/nomad "0.7.2"]
                 [clj-kafka.franzy/core nil]
                 [clj-kafka.franzy/admin nil]
                 [clj-kafka.franzy/nippy nil]
                 [clj-kafka.franzy/fressian nil]
                 [clj-kafka.franzy/json nil]
                 [clj-kafka.franzy/embedded nil]
                 [org.slf4j/slf4j-api "1.7.19"]
                 [org.slf4j/slf4j-nop "1.7.19"]
                 [log4j/log4j "1.2.17"]]
  :monolith/inherit true
  :middleware [leiningen.v/dependency-version-from-scm]

  :plugins
  [[lein-monolith "1.0.1"]
   [com.roomkey/lein-v "6.1.0-cb-9-0x521a"]
   [lein-codox "0.9.4"]]
  :codox {:metadata    {:doc/format :markdown}
          :doc-paths   ["README.md"]
          :output-path "doc/api"}
  :profiles {:dev              {:dependencies [[midje "1.7.0"]]
                                :plugins      [[lein-midje "3.2"]
                                               [lein-set-version "0.4.1"]
                                               [lein-update-dependency "0.1.2"]
                                               [lein-pprint "1.1.1"]]}
             :reflection-check {:global-vars
                                {*warn-on-reflection* true
                                 *assert*             false
                                 *unchecked-math*     :warn-on-boxed}}})
