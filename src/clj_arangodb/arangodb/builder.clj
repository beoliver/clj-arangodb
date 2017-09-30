(ns clj-arangodb.arangodb.builder
  (:require [clj-arangodb.arangodb.utils :as utils])
  (:import com.arangodb.ArangoDB$Builder))

(defn- ^ArangoDB$Builder add-host-port
  [^ArangoDB$Builder builder {:keys [^String host ^Integer port] :as options}]
  (if (or host port)
    (.host builder host (int port))
    builder))

(defn- ^ArangoDB$Builder add-protocol
  [^ArangoDB$Builder builder {:keys [protocol] :as options}]
  (if protocol
    (.useProtocol builder (utils/keyword->Protocol protocol))
    builder))

(defn- ^ArangoDB$Builder add-user
  [^ArangoDB$Builder builder {:keys [^String user] :as options}]
  (if user
    (.user builder user)
    builder))

(defn- ^ArangoDB$Builder add-password
  [^ArangoDB$Builder builder {:keys [^String password] :as options}]
  (if password
    (.password builder password)
    builder))

(defn- ^ArangoDB$Builder add-ssl-context
  [^ArangoDB$Builder builder {:keys [ssl] :as options}]
  (if ssl
    (-> builder (.sslContext ssl) (.useSsl true) )
    builder))

(defn- ^ArangoDB$Builder add-timeout
  [^ArangoDB$Builder builder {:keys [^Integer timeout] :as options}]
  (if timeout
    (.timeout (int timeout))
    builder))

(defn- ^ArangoDB$Builder add-chunksize
  [^ArangoDB$Builder builder {:keys [^Integer chunksize] :as options}]
  (if chunksize
    (.chunksize (int chunksize))
    builder))

(defn- ^ArangoDB$Builder add-max-connections
  [^ArangoDB$Builder builder {:keys [^Integer max-connections] :as options}]
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
