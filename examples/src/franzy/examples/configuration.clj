(ns franzy.examples.configuration
  (:require [franzy.admin.zookeeper.client :as client]
            [franzy.admin.zookeeper.defaults :as zk-defaults]))

(def kafka-brokers
  ;;your kafka brokers in your cluster
  ["127.0.0.1:9092"])

(def zookeeper-servers
  ;;your zk servers in your cluster
  ["127.0.0.1:2181"])

(def is-secure? false)

(def default-partition-count 4)

;;we'll bootstrap these topics
(def topics-to-create
  {;;serialize strings to this topic
   :string-topic          "savage-freds"
   ;;serialize EDN to this topic
   :edn-topic             "great-moments-in-william-atherton"
   ;;serialize nippy to this topic
   :nippy-topic           "land-wars-in-asia"
   ;;serialize fressian to this topic
   :fressian-topic        "stop-allocating-arrays-in-loops"
   ;;serialize json to this topic
   :json-topic            "hackers-is-a-dumb-word-for-programmers"
   ;;we'll delete this topic later
   :nippy-topic-to-delete "you-are-not-an-engineer"
   ;;we'll delete this topic later
   :edn-topic-to-delete   "customer-data-to-sell-illictly"})

;;convenience function to list the topics we are working with in our examples
(defn topics-list []
  (vals topics-to-create))

(def zk-config
  {:servers zookeeper-servers})

(defn make-zk-config []
  (merge (zk-defaults/zk-client-defaults) zk-config))

(defn make-zk-client []
  (client/make-zk-client (make-zk-config)))

(defn make-zk-utils []
  (client/make-zk-utils (make-zk-config) is-secure?))
