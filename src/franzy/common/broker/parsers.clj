(ns franzy.common.broker.parsers
  "Helpers for parsing connection strings in Kafka."
  (:import (java.net URI)))

;; we could do more with regexes here, ex (re-find #"(?<host>.*):(?<port>\d*)" host-and-port), look-ahead, blah blah to be fancy or possibly more efficient, but honestly no one cares
;; we mainly use URI below because it does all the validation for us and parsing without corner cases

(defn parse-servers
  "Returns a vector of server connection strings from a connection string."
  [connection-string]
  (when connection-string
    (clojure.string/split connection-string #",")))

(defn fragment->uri
  "Extracts a connection string fragment as a URI.

  Useful for further parsing/usage with other APIs."
  ^URI [host-and-port]
  (URI. (str "tcp://" host-and-port)))

(defn connection-string->uris
  "Returns a collection of URIs from a connection connection string."
  [connection-string]
  (map fragment->uri (parse-servers connection-string)))

(defn parse-host-and-port
  "Parses a host/port fragment, and extracts them as a map."
  [host-and-port]
  (let [uri (fragment->uri host-and-port)]
    {:host (.getHost uri)
     :port (.getPort uri)}))

(defn parse-port
  "Parses just the port from a host/port fragment in a connection string and returns it as a number."
  [host-and-port]
  (some-> (fragment->uri host-and-port) (.getPort)))

(defn parse-host
  "Parses just the host from a host/port fragment in a connection string and returns it as a number."
  [host-and-port]
  (some-> (fragment->uri host-and-port) (.getHost)))

(defn parse-server-list
  "Parses the servers and ports in a connection string and returns a sequence of server/port maps.

  Useful for logging and getting an overview of resource usage."
  [connection-string]
  (map parse-host-and-port (parse-servers connection-string)))

(defn parse-server-hosts
  "Returns a set of just the hosts from a connection string.

  Useful for seeing what hosts are active in a cluster."
  [connection-string]
  (into #{} (map parse-host (parse-servers connection-string))))

(defn parse-ports
  "Returns a set of the ports used by a connection string.

  Note that the ports may be open on different hosts, so this is more useful in scenarios such as testing on localhost
  or where a FQDN is used or in bridged configurations such as those used commonly by containers such as docker.

  Useful for checking what ports are open in a cluster."
  [connection-string]
  ;;this could be more efficient, but being lazy here
  (into #{} (map parse-port (parse-servers connection-string))))