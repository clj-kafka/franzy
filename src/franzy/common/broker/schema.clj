(ns franzy.common.broker.schema
  (:require [franzy.common.schema :as fs]
            [franzy.common.models.schema :as fms]
            [schema.core :as s]))

;;TODO: more generic range function for these schemas, just adding these as placeholders/legibility
(def SegmentByteInt
  ;;yes, 14 is indeed the number of the counting. why not?
  (s/both fs/PosInt (s/pred (fn [v] (>= v 14) 'segment-byte-int?))))

(def LogIndexByteInt
  ;;yes, 4 is indeed the number of the counting
  (s/both fs/PosInt (s/pred (fn [v] (>= v 4) 'log-index-byte-int?))))

;;TODO: this schema needs heavy review and tweaking, but works in cases tested so far...
(def BrokerConfig
  "Schema for a Kafka Broker.

  See http://kafka.apache.org/documentation.html#brokerconfigs"
  {(s/required-key :zookeeper.connect)                             fs/NonEmptyStringOrStringList ;;TODO: more strict schema
   (s/optional-key :advertised.host.name)                          s/Str
   (s/optional-key :advertised.listeners)                          s/Str
   (s/optional-key :advertised.port)                               fs/SPosInt
   (s/optional-key :auto.create.topics.enable)                     s/Bool
   (s/optional-key :auto.leader.rebalance.enable)                  s/Bool
   (s/optional-key :background.threads)                            fs/PosInt
   (s/required-key :broker.id)                                     fs/SPosIntWithDefault
   (s/optional-key :compression.type)                              s/Str
   (s/optional-key :delete.topic.enable)                           s/Bool
   (s/optional-key :host.name)                                     s/Str
   (s/optional-key :leader.imbalance.check.interval.seconds)       fs/PosLong
   (s/optional-key :leader.imbalance.per.broker.percentage)        fs/PosInt
   (s/optional-key :listeners)                                     s/Str
   (s/optional-key :log.dir)                                       s/Str
   (s/optional-key :log.dirs)                                      s/Str
   (s/optional-key :log.flush.interval.messages)                   fs/PosLong
   (s/optional-key :log.flush.interval.ms)                         fs/SPosLong
   (s/optional-key :log.flush.offset.checkpoint.interval.ms)       fs/SPosInt
   (s/optional-key :log.flush.scheduler.interval.ms)               fs/SPosLong
   (s/optional-key :log.retention.bytes)                           Long
   (s/optional-key :log.retention.hours)                           fs/SPosInt
   (s/optional-key :log.retention.minutes)                         fs/SPosInt
   (s/optional-key :log.retention.ms)                              fs/SPosLong
   (s/optional-key :log.roll.hours)                                fs/SPosInt
   (s/optional-key :log.roll.jitter.hours)                         fs/SPosInt
   (s/optional-key :log.roll.jitter.ms)                            fs/SPosLong
   (s/optional-key :log.roll.ms)                                   fs/SPosLong
   (s/optional-key :log.segment.bytes)                             SegmentByteInt
   (s/optional-key :log.segment.delete.delay.ms)                   fs/SPosLong
   (s/optional-key :message.max.bytes)                             fs/SPosInt
   (s/optional-key :min.insync.replicas)                           fs/PosInt
   (s/optional-key :num.io.threads)                                fs/PosInt
   (s/optional-key :num.network.threads)                           fs/PosInt
   (s/optional-key :num.recovery.threads.per.data.dir)             fs/PosInt
   (s/optional-key :num.replica.fetchers)                          fs/PosInt
   (s/optional-key :offset.metadata.max.bytes)                     fs/SPosInt
   (s/optional-key :offsets.commit.required.acks)                  fs/SPosShortWithDefault
   (s/optional-key :offsets.commit.timeout.ms)                     fs/PosInt
   (s/optional-key :offsets.load.buffer.size)                      fs/PosInt
   (s/optional-key :offsets.retention.check.interval.ms)           fs/PosLong
   (s/optional-key :offsets.retention.minutes)                     fs/PosInt
   (s/optional-key :offsets.topic.compression.codec)               fs/SPosInt
   (s/optional-key :offsets.topic.num.partitions)                  fs/PosInt
   (s/optional-key :offsets.topic.replication.factor)              fs/PosShort
   (s/optional-key :offsets.topic.segment.bytes)                   fs/PosInt
   (s/optional-key :port)                                          fs/SPosInt
   (s/optional-key :queued.max.requests)                           fs/PosInt
   (s/optional-key :quota.consumer.default)                        fs/PosLong
   (s/optional-key :quota.producer.default)                        fs/PosLong
   (s/optional-key :replica.fetch.max.bytes)                       fs/SPosInt
   (s/optional-key :replica.fetch.min.bytes)                       fs/SPosInt
   (s/optional-key :replica.fetch.wait.max.ms)                     fs/SPosInt
   (s/optional-key :replica.high.watermark.checkpoint.interval.ms) fs/SPosLong
   (s/optional-key :replica.lag.time.max.ms)                       fs/SPosLong
   (s/optional-key :replica.socket.receive.buffer.bytes)           fs/SPosInt
   (s/optional-key :replica.socket.timeout.ms)                     fs/SPosInt
   (s/optional-key :request.timeout.ms)                            fs/SPosInt
   (s/optional-key :socket.receive.buffer.bytes)                   fs/SPosInt
   (s/optional-key :socket.request.max.bytes)                      fs/PosInt
   (s/optional-key :socket.send.buffer.bytes)                      fs/SPosInt
   (s/optional-key :unclean.leader.election.enable)                s/Bool
   (s/optional-key :zookeeper.connection.timeout.ms)               fs/SPosInt
   (s/optional-key :zookeeper.session.timeout.ms)                  fs/SPosInt
   (s/optional-key :zookeeper.set.acl)                             s/Bool
   (s/optional-key :broker.id.generation.enable)                   s/Bool
   (s/optional-key :connections.max.idle.ms)                       fs/SPosLong
   (s/optional-key :controlled.shutdown.enable)                    s/Bool
   (s/optional-key :controlled.shutdown.max.retries)               fs/SPosInt
   (s/optional-key :controlled.shutdown.retry.backoff.ms)          fs/SPosLong
   (s/optional-key :controller.socket.timeout.ms)                  fs/SPosInt
   (s/optional-key :default.replication.factor)                    fs/SPosInt
   (s/optional-key :fetch.purgatory.purge.interval.requests)       fs/SPosInt
   (s/optional-key :group.max.session.timeout.ms)                  fs/SPosInt
   (s/optional-key :group.min.session.timeout.ms)                  fs/SPosInt
   (s/optional-key :inter.broker.protocol.version)                 s/Str
   (s/optional-key :log.cleaner.backoff.ms)                        fs/SPosLong
   (s/optional-key :log.cleaner.dedupe.buffer.size)                fs/SPosLong
   (s/optional-key :log.cleaner.delete.retention.ms)               fs/SPosLong
   (s/optional-key :log.cleaner.enable)                            s/Bool
   (s/optional-key :log.cleaner.io.buffer.load.factor)             fs/SPosDouble
   (s/optional-key :log.cleaner.io.buffer.size)                    fs/SPosInt
   (s/optional-key :log.cleaner.io.max.bytes.per.second)           fs/SPosDouble
   (s/optional-key :log.cleaner.min.cleanable.ratio)               fs/SPosDouble
   (s/optional-key :log.cleaner.threads)                           fs/PosInt
   (s/optional-key :log.cleanup.policy)                            s/Str
   (s/optional-key :log.index.interval.bytes)                      fs/SPosInt
   (s/optional-key :log.index.size.max.bytes)                      LogIndexByteInt
   (s/optional-key :log.preallocate)                               s/Bool
   (s/optional-key :log.retention.check.interval.ms)               fs/PosLong
   (s/optional-key :max.connections.per.ip)                        fs/PosInt
   (s/optional-key :max.connections.per.ip.overrides)              s/Str
   (s/optional-key :num.partitions)                                fs/PosInt
   (s/optional-key :principal.builder.class)                       s/Str ;;TODO: accepts only class/class as string
   (s/optional-key :producer.purgatory.purge.interval.requests)    fs/SPosInt
   (s/optional-key :replica.fetch.backoff.ms)                      fs/SPosInt
   (s/optional-key :reserved.broker.max.id)                        fs/SPosInt
   (s/optional-key :sasl.kerberos.kinit.cmd)                       s/Str
   (s/optional-key :sasl.kerberos.min.time.before.relogin)         fs/SPosLong
   (s/optional-key :sasl.kerberos.principal.to.local.rules)        fs/StringOrList ;;TODO: list values
   (s/optional-key :sasl.kerberos.service.name)                    s/Str
   (s/optional-key :sasl.kerberos.ticket.renew.jitter)             fs/SPosDouble
   (s/optional-key :sasl.kerberos.ticket.renew.window.factor)      fs/PosDouble
   (s/optional-key :security.inter.broker.protocol)                fms/SecurityProtocolEnum
   (s/optional-key :ssl.cipher.suites)                             fs/StringOrList
   (s/optional-key :ssl.client.auth)                               s/Str
   (s/optional-key :ssl.enabled.protocols)                         fs/StringOrList
   (s/optional-key :ssl.key.password)                              s/Str
   (s/optional-key :ssl.keymanager.algorithm)                      s/Str
   (s/optional-key :ssl.keystore.location)                         s/Str
   (s/optional-key :ssl.keystore.password)                         s/Str
   (s/optional-key :ssl.keystore.type)                             s/Str
   (s/optional-key :ssl.protocol)                                  fms/SecurityProtocolEnum
   (s/optional-key :ssl.provider)                                  s/Str
   (s/optional-key :ssl.trustmanager.algorithm)                    s/Str
   (s/optional-key :ssl.truststore.location)                       s/Str ;;TODO: list values
   (s/optional-key :ssl.truststore.password)                       s/Str
   (s/optional-key :ssl.truststore.type)                           s/Str
   (s/optional-key :authorizer.class.name)                         fs/SPosLong
   (s/optional-key :metric.reporters)                              fs/StringOrList ;;TODO: only accepts a list of classes
   (s/optional-key :metrics.num.samples)                           fs/PosInt
   (s/optional-key :metrics.sample.window.ms)                      fs/PosLong
   (s/optional-key :quota.window.num)                              fs/PosInt
   (s/optional-key :quota.window.size.seconds)                     fs/PosInt
   (s/optional-key :ssl.endpoint.identification.algorithm)         s/Str
   (s/optional-key :zookeeper.sync.time.ms)                        fs/SPosInt})
