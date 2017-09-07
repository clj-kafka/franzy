(ns franzy.common.async.wrappers
  (:import (java.util.concurrent Future)
           (clojure.lang IPending IBlockingDeref IDeref)))

;;TODO: revisit, this is probably unnecessary but added quickly to ensure that any futures from Kafka don't need to be tamped with to work seamlessly.
(defn wrap-future
  "Wraps a Java Future and applies a function when the result is realized.

  Useful for cases where type conversion needs to happen to avoid leaking implementation details back to consumers of the future.
  Implements all the standard protocols of a Clojure Future, allowing convenient use in existing code."
  [^Future fut f]
  (reify
    IDeref
    (deref [_]
      (f (deref fut)))
    IBlockingDeref
    (deref
      [_ timeout-ms timeout-val]
      (f (deref fut timeout-ms timeout-val)))
    IPending
    (isRealized [_] (.isDone fut))
    Future
    (get [_] (f (.get fut)))
    (get [_ timeout unit] (f (.get fut timeout unit)))
    (isCancelled [_] (.isCancelled fut))
    (isDone [_] (.isDone fut))
    (cancel [_ interrupt?] (.cancel fut interrupt?))))
