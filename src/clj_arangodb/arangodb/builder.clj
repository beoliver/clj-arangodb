(ns clj-arangodb.arangodb.builder
  (:require [clj-arangodb.arangodb.utils :as utils])
  (:import com.arangodb.ArangoDB$Builder))

(defn- add-host-port [builder {:keys [host port] :as options}]
  (if (or host port)
    (.host builder host (int port))
    builder))

(defn- add-protocol [builder {:keys [protocol] :as options}]
  (if protocol
    (.useProtocol builder (utils/keyword->Protocol protocol))
    builder))

(defn- add-user [builder {:keys [user] :as options}]
  (if user
    (.user builder user)
    builder))

(defn- add-password [builder {:keys [password] :as options}]
  (if password
    (.password builder password)
    builder))

(defn- add-ssl-context [builder {:keys [ssl] :as options}]
  (if ssl
    (-> builder (.sslContext ssl) (.useSsl true) )
    builder))

(defn- add-timeout [builder {:keys [timeout] :as options}]
  (if timeout
    (.timeout (int timeout))
    builder))

(defn- add-chunksize [builder {:keys [chunksize] :as options}]
  (if chunksize
    (.chunksize (int chunksize))
    builder))

(defn- add-max-connections [builder {:keys [max-connections] :as options}]
  (if max-connections
    (.maxConnections (int max-connections))
    builder))

(defn build-new-arango-db
  [options]
  (.build (-> (new ArangoDB$Builder)
              (add-host-port options)
              (add-protocol options)
              (add-user options)
              (add-password options)
              (add-ssl-context options)
              (add-timeout options)
              (add-chunksize options)
              (add-max-connections options))))
