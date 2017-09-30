(ns clj-arangodb.arangodb.core
  (:require [clj-arangodb.arangodb.utils :as utils])
  (:import
   com.arangodb.ArangoDB$Builder
   com.arangodb.ArangoDB
   com.arangodb.ArangoDatabase
   com.arangodb.ArangoDBException))

;;; This namespace includes all functions that take an ArangoDB object as the
;;; first parameter.
;;; Included is the function `connect` that creates a connection to a database


;;; builders that are threaded on a call to `connect`.
;;; the options map is checked for relevant keywords
;;; if none are found then each function is ignored
;;; (falling back to the default values)

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
  [^ArangoDB$Builder builder {:keys [^SSLContext ssl] :as options}]
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

(defn ^ArangoDB connect
  "Takes an optional map that may contain the following:
  :host String
  :port Integer | Long
  :user String
  :password String
  :protocol :vst | :http-json | :http-vpack (:vst by default)
  :ssl SSlContext
  :timeout Integer | Long
  :chunksize Integer | Long
  :max-connections Integer | Long
  If no options are passed - the defaults of the java-driver are used:
  https://github.com/arangodb/arangodb-java-driver
  "
  ([]
   (connect {}))
  ([{:keys [host port user password] :as options}]
   (.build (-> (new ArangoDB$Builder)
               (add-host-port options)
               (add-protocol options)
               (add-user options)
               (add-password options)
               (add-ssl-context options)
               (add-timeout options)
               (add-chunksize options)
               (add-max-connections options)))))

(defn ^Boolean create-db
  "returns `true` on success else `ArangoDBException`"
  [^ArangoDB conn ^String db-name]
  (.createDatabase conn db-name))

(defn ^ArangoDatabase get-db
  "Always returns a new `ArrangoDatabase` even if no such database exists
  the returned object can be used if a databse is created at a later time"
  [^ArangoDB conn ^String db-name]
  (-> conn (.db db-name)))

(defn get-dbs
  "returns a `seq` of strings corresponding to the names of databases"
  [^ArangoDB conn] (seq (.getDatabases conn)))

(defn drop-db
  "returns `true` if database with `db-name` was dropped else `ArangoDBException`"
  [^ArangoDB conn ^String db-name]
  (-> conn (.db db-name) .drop))

(defn drop-db-if-exists
  "returns `true` if database with `db-name` was dropped else `nil`.
  Usefull for testing when you dont want to worry about try catch."
  [^ArangoDB conn ^String db-name]
  (try (drop-db conn db-name)
       (catch ArangoDBException e nil)))
