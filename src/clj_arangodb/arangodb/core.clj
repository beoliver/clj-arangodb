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
  [^ArangoDB$Builder builder {:keys [useProtocol] :as options}]
  (if useProtocol
    (.useProtocol builder (utils/keyword->Protocol useProtocol))
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
  [^ArangoDB$Builder builder {:keys [^SSLContext sslContext] :as options}]
  (if sslContext
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
  [^ArangoDB$Builder builder {:keys [^Integer maxConnections] :as options}]
  (if maxConnections
    (.maxConnections (int maxConnections))
    builder))

(defn ^ArangoDB connect
  "Takes an optional map that may contain the following:
   keys have the same names as the methods in the java-driver (makes documention easier)
  :host a String default is '127.0.0.1'
  :port an Integer or Long default is 8529
  :user a String default is 'root'
  :password String by default no password is used
  :useProtocol :vst | :http-json | :http-vpack (:vst by default)
  :sslContext SSlContext not used
  :timeout Integer | Long
  :chunksize Integer | Long
  :maxConnections Integer | Long
  If no options are passed - the defaults of the java-driver are used:
  https://github.com/arangodb/arangodb-java-driver
  "
  ([]
   (connect {}))
  ([{:keys [host port user password protocol ssl timeout chunksize maxConnections] :as options}]
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

(defn ^ArangoDatabase create-and-get-db
  "returns an `ArrangoDatabase` handler. This is just short hand for create-db and get-db"
  [^ArangoDB conn ^String db-name]
  (do (create-db conn db-name)
      (get-db conn db-name)))

(defn get-dbs
  "returns a `seq` of strings corresponding to the names of databases"
  [^ArangoDB conn] (seq (.getDatabases conn)))

(defn ^Boolean drop-db
  "returns `true` if database with `db-name` was dropped else `ArangoDBException`"
  [^ArangoDB conn ^String db-name]
  (-> conn (.db db-name) .drop))

(defn ^Boolean drop-db-if-exists
  "returns `true` if database with `db-name` was dropped else `false`.
  Usefull for testing when you dont want to worry about try catch."
  [^ArangoDB conn ^String db-name]
  (try (drop-db conn db-name)
       (catch ArangoDBException e false)))


(defn map-to-base-document

  )
