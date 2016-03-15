(ns franzy.embedded.core-test
  (:require [midje.sweet :refer :all]
            [clojure.java.io :as io]
            [nomad :refer [defconfig]]
            [franzy.embedded.defaults :as defaults]
            [franzy.embedded.broker :as broker]
            [franzy.embedded.component :as broker-component]))

;;TODO: decent integration testing configs, using this is as a quick fix
;; Put this in resources/config with your settings for example
;{:nomad/hosts
; {"your-host"
;  {:kakfa/broker-config
;   {:host.name                            "127.0.0.1"
;    :port                                 9092
;    :broker.id                            0
;    ;:advertised.host.name "your-insignificant-host.com"
;    ;:advertised.port 9092
;    :num.partitions                       1
;    :controller.socket.timeout.ms         1500
;    :controlled.shutdown.enable           true
;    :replica.socket.timeout.ms            1500
;    :delete.topic.enable                  true
;    :controlled.shutdown.retry.backoff.ms 100
;    :log.cleaner.dedupe.buffer.size       2097152
;    :zookeeper.connect                    "127.0.0.1:2181"}}}}

(defconfig broker-test-config (io/resource "config/broker.edn"))

(defn make-broker-config []
  (let [broker-conf (get-in (broker-test-config) [:kakfa/broker-config])]
    ;;safely set a new log directory...
    (println broker-conf)
    (assoc broker-conf :log.dir (defaults/make-default-log-directory))))

(defn make-test-broker []
  (-> (make-broker-config)
      (broker/make-broker)))

(defn make-test-startable-broker []
  (-> (make-broker-config)
      (broker/make-startable-broker)))

(defn make-test-broker-component []
  (-> (make-broker-config)
      (broker-component/make-embedded-broker)))
