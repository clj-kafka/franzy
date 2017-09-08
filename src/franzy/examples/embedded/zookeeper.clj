(ns franzy.examples.embedded.zookeeper
  (:require [travel-zoo.embedded.server :as server]
            [travel-zoo.embedded.protocols :refer :all])
  (:import (travel_zoo.embedded.server EmbeddedZookeeperServer)))

(def zk-server
  (server/make-embedded-zookeeper {:port 2181}))

(defn start-embedded-zk []
  (start-zk zk-server))

(defn stop-embedded-zk []
  (stop-zk zk-server))

(defn close-embedded-zk []
  (.close ^EmbeddedZookeeperServer zk-server))

