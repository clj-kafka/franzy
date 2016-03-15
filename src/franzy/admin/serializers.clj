(ns franzy.admin.serializers
  (:import (kafka.utils ZKStringSerializer$)
           (org.I0Itec.zkclient.serialize ZkSerializer)))

(defn ^ZKStringSerializer$ zk-string-serializer
  "Creates a UTF-8 String Zookeeper Serializer."
  ^ZkSerializer []
  (ZKStringSerializer$/MODULE$))
