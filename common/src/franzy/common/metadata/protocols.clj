(ns franzy.common.metadata.protocols)

(defprotocol KafkaMeasurable
  "Protocol for returning metrics from Kafka."
  (metrics [this]))

(defprotocol TopicMetadataProvider
  "Protocol for providing metadata about topics."
  (list-topics [this])
  ;;It would be nice to include this here, but for some reason Kafka Java API decided not to expose listTopics on the producer.
  ;;Boo. hiss.
  ;(partitions-for [this topic])
  )

(defprotocol PartitionMetadataProvider
  "Protocol for providing metadata about partitions."
  ;;Note: see above, prefer to merge this with the TopicMetadataProvider protocol but inconsistencies in the Java API.
  (partitions-for [this topic]))
