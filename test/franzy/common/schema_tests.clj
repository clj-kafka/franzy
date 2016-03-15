(ns franzy.common.schema-tests
  (:require [midje.sweet :refer :all]
            [schema.core :as s]
            [franzy.common.schema :as fs]))

;(namespace-state-changes [(before :facts (s/set-fn-validation! true))
;                          (after :facts (s/set-fn-validation! false))])

;;A wise poet once said, "I write these tests to seem like there are a lot of tests, but truth is I was just in a REPL session."

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Tests to ensure that was the schema involves, we don't murder Kafka.
;; While these tests are perhaps repetitive and overlapping, we test schema more and more aggressively as time goes on.
;; In the real-world, someone can and will make a giant mistake. The problem is Kafka, especially as features are added
;; will happily accept our bad data. Putting a distributed system in an undefined is not fun, and thus, this mess.
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(facts
  "Numbers that must to be coerced to double must pass schema validation."
  (fact
    "One should be coeracble to double."
    (s/validate fs/CoercableDouble 1)
    => 1)
  (fact
    "Double max value is coercable to double."
    (s/check fs/CoercableDouble Double/MAX_VALUE)
    => nil)
  (fact
    "Double min value is coercable to double."
    (s/check fs/CoercableDouble Double/MIN_VALUE)
    => nil)
  (fact
    "Strings are not coercable to double."
    (s/check fs/CoercableDouble "Free salad course.")
    =not=> nil))

(facts
  "Numbers that must be coerced to int must pass schema validation."
  (fact
    "Integer max value is coercable to int."
    (s/check fs/CoercableInt Integer/MAX_VALUE)
    => nil)
  (fact
    "Integer min value is coercable to int."
    (s/check fs/CoercableInt Integer/MIN_VALUE)
    => nil)
  (fact
    "Integer overflowing value is not coercable to int."
    (s/check fs/CoercableInt (+ 1 Integer/MAX_VALUE))
    =not=> nil)
  (fact
    "Integer underflowing value is not coercable to int."
    (s/check fs/CoercableInt (- Integer/MIN_VALUE 1))
    =not=> nil)
  (fact
    "Strings are not coercable to int."
    (s/check fs/CoercableInt "lord of 3 cheeses.")
    =not=> nil))

(facts
  "Numbers that must be coerced to long must pass schema validation (or death)."
  (fact
    "Long max value is coercable to long."
    (s/check fs/CoercableLong Long/MAX_VALUE)
    => nil)
  (fact
    "Long min value is coercable to long."
    (s/check fs/CoercableLong Long/MIN_VALUE)
    => nil)
  (fact
    "Long overflowing value is not coercable to long."
    (s/check fs/CoercableLong (+ 1 Long/MAX_VALUE))
    =not=> nil)
  (fact
    "Long underflowing value is not coercable to long."
    (s/check fs/CoercableLong (- Long/MIN_VALUE 1))
    =not=> nil)
  (fact
    "Strings are not coercable to long."
    (s/check fs/CoercableLong "3 AM at Denny's.")
    =not=> nil))

(facts
  "Numbers that must be coerced to short must pass schema validation."
  ;;be short, be proud
  (fact
    "Short max value is coercable to short."
    (s/check fs/CoercableShort Short/MAX_VALUE)
    => nil)
  (fact
    "Short min value is coercable to short."
    (s/check fs/CoercableShort Short/MIN_VALUE)
    => nil)
  (fact
    "Long overflowing value is not coercable to long."
    (s/check fs/CoercableShort (+ 1 Short/MAX_VALUE))
    =not=> nil)
  (fact
    "Long underflowing value is not coercable to long."
    (s/check fs/CoercableShort (- Short/MIN_VALUE 1))
    =not=> nil)
  (fact
    "Strings are not coercable to short."
    (s/check fs/CoercableShort "Taco revenge.")
    =not=> nil))

(facts
  "Functions must be able to be validated."
  (fact
    "A function is a function."
    ;;zen-drive engaged
    (s/check fs/Function identity)
    => nil)
  (fact
    "A string is not a function."
    (s/check fs/Function "chicken and rice are the best of friends.")
    =not=> nil))

(facts
  "Things that are not nil must be able to be validated."
  (fact
    "Things are not nil must pass schema validation."
    (s/check fs/AnyButNil 1) => nil
    (s/check fs/AnyButNil "") => nil
    (s/check fs/AnyButNil true) => nil
    (s/check fs/AnyButNil false) => nil
    (s/check fs/AnyButNil :meow) => nil
    (s/check fs/AnyButNil []) => nil
    (s/check fs/AnyButNil '()) => nil
    (s/check fs/AnyButNil {}) => nil)
  (fact
    "Things that are nil must fail schema validation."
    ;;feeling lonely for now...
    (s/check fs/AnyButNil nil) =not=> nil))

(facts
  "Things that are greater than or equal to zero are indeed."
  (fact
    "Zero is greater than or equal to zero."
    (s/check fs/GreaterThanOrEqualToZero 0)
    => nil)
  (fact
    "One is greater than or equal to zero."
    ;;We discriminate
    (s/check fs/GreaterThanOrEqualToZero 1)
    => nil)
  (fact
    "A negative number is not greater than or equal to zero."
    (s/check fs/GreaterThanOrEqualToZero -1)
    =not=> nil))

(facts
  "Positive integers must pass schema validation."
  (fact
    "One is a positive integer."
    (s/check fs/PosInt 1)
    => nil)
  (fact
    "Zero is not a positive integer."
    (s/check fs/PosInt 0)
    =not=> nil)
  (fact
    "Negative one is not a positive integer."
    (s/check fs/PosInt -1)
    =not=> nil))

(facts
  "Positive longs must pass schema validation."
  (fact
    "One is a positive long."
    (s/check fs/PosLong 1)
    => nil)
  (fact
    "Zero is not a positive long."
    (s/check fs/PosLong 0)
    =not=> nil)
  (fact
    "Negative one is not a positive long."
    (s/check fs/PosLong -1)
    =not=> nil))

(facts
  "Positive shorts must pass schema validation."
  (fact
    "One is a positive short."
    (s/check fs/PosShort 1)
    => nil)
  (fact
    "Zero is not a positive short."
    (s/check fs/PosShort 0)
    =not=> nil)
  (fact
    "Negative one is not a positive short."
    (s/check fs/PosShort -1)
    =not=> nil))

(facts
  "Positive doubles must pass schema validation."
  (fact
    "One is a positive double."
    (s/check fs/PosDouble 1)
    => nil)
  (fact
    "Zero is not a positive double."
    (s/check fs/PosDouble 0)
    =not=> nil)
  (fact
    "Negative one is not a positive double."
    (s/check fs/PosDouble -1)
    =not=> nil))


(facts
  "Positive integers must pass zero-inclusive schema validation."
  (fact
    "One is a positive integer."
    (s/check fs/SPosInt 1)
    => nil)
  (fact
    "Zero is a positive zero-inclusive integer."
    (s/check fs/SPosInt 0)
    => nil)
  (fact
    "Negative one is not a positive integer."
    (s/check fs/SPosInt -1)
    =not=> nil))

(facts
  "Positive longs must pass zero-inclusive schema validation."
  (fact
    "One is a positive long."
    (s/check fs/SPosLong 1)
    => nil)
  (fact
    "Zero is a positive zero-inclusive long."
    (s/check fs/SPosLong 0)
    => nil)
  (fact
    "Negative one is not a positive long."
    (s/check fs/SPosLong -1)
    =not=> nil))

(facts
  "Positive shorts must pass zero-inclusive schema validation."
  (fact
    "One is a positive short."
    (s/check fs/SPosShort 1)
    => nil)
  (fact
    "Zero is a zero-inclusive positive short."
    (s/check fs/SPosShort 0)
    => nil)
  (fact
    "Negative one is not a positive short."
    (s/check fs/SPosShort -1)
    =not=> nil))

(facts
  "Positive doubles must pass zero-inclusive schema validation."
  (fact
    "One is a positive double."
    (s/check fs/SPosDouble 1)
    => nil)
  (fact
    "Zero is a zero-inclusive positive double."
    (s/check fs/SPosDouble 0)
    => nil)
  (fact
    "Negative one is not a positive double."
    (s/check fs/SPosDouble -1)
    =not=> nil))

(facts
  "A schema that takes a string or list must pass schema validation."
  (fact
    "A string is a string or list."
    (s/check fs/StringOrList "feet!")
    => nil)
  (fact
    "A string list is a string or list."
    (s/check fs/StringOrList ["8" "6" "7" "5" "3" "O" "9"])
    => nil)
  (fact
    "A list of integer is a string or list"
    (s/check fs/StringOrList [8 6 7 5 3 0 9])
    => nil)
  (fact
    "A number is not a string or list."
    (s/check fs/StringOrList 8675309)
    =not=> nil))

(facts
  "A schema that takes a string or string list must pass schema validation."
  (fact
    "A string is a string or list."
    (s/check fs/StringOrStringList "more feet!")
    => nil)
  (fact
    "A string list is a string or string list."
    (s/check fs/StringOrStringList ["8" "6" "7" "5" "3" "O" "9"])
    => nil)
  (fact
    "A list of integer is not a string or list"
    (s/check fs/StringOrStringList [8 6 7 5 3 0 9])
    =not=> nil)
  (fact
    "A number is not a string or list."
    (s/check fs/StringOrStringList 8675309)
    =not=> nil))

(facts
  "Negative default numbers should pass schema validation."
  (fact
    "A negative one is a valid default."
    (s/check fs/NegativeDefaultNumber -1) => nil)
  (fact
    "Any positive number, zero-inclusive is a valid default."
    (s/check fs/NegativeDefaultNumber 1) => nil
    (s/check fs/NegativeDefaultNumber Integer/MAX_VALUE) => nil
    (s/check fs/NegativeDefaultNumber Long/MAX_VALUE) => nil
    (s/check fs/NegativeDefaultNumber Double/MAX_VALUE) => nil
    (s/check fs/NegativeDefaultNumber 0) => nil)
  (fact
    "Any negative number other than negative one is invalid."
    (s/check fs/NegativeDefaultNumber -2) =not=> nil
    (s/check fs/NegativeDefaultNumber Long/MIN_VALUE) =not=> nil)
  (fact
    "Any input that is not a number is invalid."
    (s/check fs/NegativeDefaultNumber "guru meditation is not something I want to mediate upon") =not=> nil
    (s/check fs/NegativeDefaultNumber :roar-my-lion) =not=> nil))
