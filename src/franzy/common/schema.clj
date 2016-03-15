(ns franzy.common.schema
  "Basic schemas for validating Kafka configs, types, functions, etc."
  (:require [schema.core :as s]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Basic validations - many of these can probably be replaced by things inside Schema...ongoing struggle
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(def Function
  "Schema for a target that must be a function."
  (s/pred fn? 'fn?))

(def AnyButNil
  "Schema for a value that can be anything but nil."
  (s/pred some? 'not-nil?))

(def NonEmptyString
  "Schema for a string that cannot be blank."
  (s/both s/Str (s/pred (complement clojure.string/blank?) 'blank?)))

(def GreaterThanOrEqualToZero
  "Schema for a number greater than or equal to zero."
  (s/pred (partial <= 0) 'GreaterThanOrEqualToZero?))

(def Positive
  "Schema for a positive Number."
  (s/pred pos? 'pos?))

;;Following "Java" schemas below are for general interop.
;; Clojure will coerce to the right type, warn, throw, etc., however when dealing with interop, we don't want any fun surprises.
;; Moreover, we also would like a friendly message why, rather than from the compiler or further down in a stack trace.
;; Likewise, we want to ensure when a schema is checked that we know a value is out of range, especially for a particular config-related value.
(def CoercableInt
  "Schema for a value that is coercable without overflow to a Integer."
  (s/both s/Int (s/pred #(<= Integer/MIN_VALUE % Integer/MAX_VALUE) 'coercable-int?)))

(def CoercableLong
  "Schema for a value that is coercable without overflow to a Long."
  (s/both s/Int (s/pred #(<= Long/MIN_VALUE % Long/MAX_VALUE) 'coercable-long?)))

(def CoercableShort
  "Schema for a value that is coercable without overflow to a Short."
  (s/both s/Int (s/pred #(<= Short/MIN_VALUE % Short/MAX_VALUE) 'coercable-short?)))

(def CoercableDouble
  "Schema for a value that is coercable without overflow to a Double."
  s/Num)

(def NegativeDefaultNumber
  "Schema for defaults that assume -1 as a null/not set. This schema is mainly for composition with other Schemas."
  (s/pred (partial <= -1) 'snegativedefaultnumber?))

(def PosInt
  "Schema for positive integers."
  (s/both Positive CoercableInt))

(def SPosInt
  "Schema for positive, zero inclusive integers."
  (s/both GreaterThanOrEqualToZero CoercableInt))

(def SPosIntWithDefault
  "Schema for positive, zero inclusive integers that can also have -1 as a default value."
  (s/either SPosInt NegativeDefaultNumber))

(def PosLong
  "Schema for positive longs."
  (s/both Positive CoercableLong))

(def SPosLong
  "Schema for positive, zero inclusive longs."
  (s/both GreaterThanOrEqualToZero CoercableLong))

(def SPosLongWithDefault
  "Schema for positive, zero inclusive longs that can also have -1 as a default value."
  (s/either SPosLong NegativeDefaultNumber))

(def PosShort
  "Schema for positive shorts."
  (s/both Positive CoercableShort))

(def SPosShort
  "Schema for positive, zero inclusive shorts."
  (s/both GreaterThanOrEqualToZero CoercableLong))

(def SPosShortWithDefault
  "Schema for positive, zero inclusive shorts that can have also have -1 as a default value."
  (s/either SPosShort NegativeDefaultNumber))

(def PosDouble
  "Schema for positive doubles."
  (s/both Positive CoercableDouble))

(def SPosDouble
  "Schema for positive, zero inclusive doubles."
  (s/both GreaterThanOrEqualToZero CoercableDouble))

(def SPosDoubleWithDefault
  "Schema for positive, zero inclusive doubles that can have also have -1 as a default value."
  (s/either SPosDouble NegativeDefaultNumber))

(def NamespacedKeyword
  "Schema for keywords that must be namespaced, i.e. :my-namespace/my-keyword"
  (s/pred (fn [kw]
            (and (keyword? kw)
                 (namespace kw)))
          'keyword-namespaced?))

(def NotEmpty
  "Schema for collections that must not be empty."
  (s/pred (complement empty?) 'not-empty?))

(def StringOrList
  "Schema for a value that can be a string or a collection."
  (s/either NonEmptyString [AnyButNil]))

(def NonEmptyStringOrList
  "Schema for a value that can be a non-empty string or collection."
  (s/either NonEmptyString (s/both NotEmpty [AnyButNil])))

(def StringOrStringList
  "Schema for a value that can be a string or string collection."
  (s/either NonEmptyString [NonEmptyString]))

(def NonEmptyStringOrStringList
  "Schema for a value that can be a non-empty string or string collection."
  (s/either NonEmptyString (s/both NotEmpty [NonEmptyString])))

