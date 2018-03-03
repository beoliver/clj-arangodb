(ns clj-arangodb.arangodb.core
  ""
  (:require
   [clojure.set :as set]
   [clj-arangodb.arangodb.utils :as utils]
   [clj-arangodb.arangodb.collections :as collections])
  (:import
   com.arangodb.ArangoDB$Builder
   com.arangodb.ArangoDB
   com.arangodb.ArangoDatabase
   com.arangodb.ArangoCollection
   com.arangodb.ArangoDBException
   com.arangodb.entity.CollectionEntity
   com.arangodb.model.CollectionCreateOptions
   com.arangodb.velocypack.VPackSlice))

;;; This namespace includes all functions that take an ArangoDB object as the
;;; first parameter.
;;; Included is the function `connect` that creates a connection to a database

;;; builders that are threaded on a call to `connect`.
;;; the options map is checked for relevant keywords
;;; if none are found then each function is ignored
;;; (falling back to the default values)

(defn- ^ArangoDB$Builder add-host-port
  [^ArangoDB$Builder builder {:keys [^String host ^Integer port] :as options}]
  (if (and host port)
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
    (-> builder (.sslContext sslContext) (.useSsl true) )
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

(defn get-db-names
  "returns a `seq` of strings corresponding to the names of databases"
  [^ArangoDB conn] (seq (.getDatabases conn)))

(defn db-exists?
  "returns true if `db-name` is an existsing db"
  [^ArangoDB conn ^String db-name]
  (boolean (some #{db-name} (get-db-names conn))))

(defn ^Boolean drop-db
  "returns `true` if database with `db-name` was dropped else `ArangoDBException`"
  [^ArangoDB conn ^String db-name]
  (-> conn (.db db-name) .drop))

(defn ^Boolean drop-db-if-exists
  "returns `true` if database with `db-name` was dropped else `nil`.
  Usefull for testing when you dont want to worry about try catch."
  [^ArangoDB conn ^String db-name]
  (try (drop-db conn db-name)
       (catch ArangoDBException e nil)))

(defn ^ArangoCollection get-collection
  "Returns a handler of the collection by the given name
  Always returns a new `ArrangoCollection` even if no such collection exists.
  The returned object can be used if a collection is created at a later time"
  ([^ArangoDB conn ^String db-name ^String coll-name]
   (get-collection (.db conn db-name) coll-name))
  ([^ArangoDatabase db ^String coll-name]
   (.collection db coll-name)))

(defn get-collections
  "returns a `lazySeq` of maps with keys
  `:id`, `:isSystem`, `:isVolatile`, `:name`, `:status`, `:type`, `:waitForSync`.
  The returned values are not handles"
  ([^ArangoDB conn ^String db-name]
   (get-collections (.db conn db-name)))
  ([^ArangoDatabase db] (map #(-> % bean
                                  (dissoc :class)
                                  (update :status str)
                                  (update :type str))
                             (.getCollections db))))

(defn collection-exists? ^Boolean
  ([^ArangoDB conn ^String db-name ^String coll-name]
   (collection-exists? (.db conn db-name) coll-name))
  ([^ArangoDatabase db ^String coll-name]
   (boolean (some #{coll-name} (map :name (get-collections db))))))

(defn create-collection
  "create a new collection entity.
  `options` is clojure `map` that will be converted into an `CollectionCreateOptions`.
  by a call to `collections/create-options`"
  ([^ArangoDB conn ^String db-name ^String coll-name options]
   (create-collection (.db conn db-name) coll-name options))
  ([^ArangoDatabase db ^String coll-name options]
   (-> (.createCollection db coll-name (collections/create-options options))
       bean
       (dissoc :class))))

(defn create-vertex-collection
  "create a new vertex collection using default options."
  ([^ArangoDB conn ^String db-name ^String coll-name]
   (create-vertex-collection (.db conn db-name) coll-name))
  ([^ArangoDatabase db ^String coll-name]
   (.createCollection db coll-name nil)))

(defn create-edge-collection
  "create a new vertex collection using default options."
  ([^ArangoDB conn ^String db-name ^String coll-name]
   (create-edge-collection (.db conn db-name) coll-name))
  ([^ArangoDatabase db ^String coll-name]
   (create-collection db coll-name {:type :edges})))

(defn drop-collection
  ""
  ([^ArangoDB conn ^String db-name ^String coll-name]
   (drop-collection (.db conn db-name) coll-name))
  ([^ArangoDatabase db ^String coll-name]
   (-> db (.collection coll-name) .drop)))

(defn insert-doc
  "works with VelocyPack (VPackSlice) and Json (String).
  returns a map with keys `:_id` `:_key` `:_new` and `:_rev`"
  ([^ArangoDB conn ^String db ^String coll-name doc]
   (insert-doc (get-collection conn db coll-name) doc))
  ([^ArangoDatabase db ^String coll-name doc]
   (insert-doc (get-collection db coll-name) doc))
  ([^ArangoCollection coll doc]
   (-> (.insertDocument coll doc)
       bean
       (dissoc :class)
       (set/rename-keys {:id :_id :key :_key :new :_new :rev :_rev}))))

(defn insert-docs
  "works with VelocyPack (VPackSlice) and Json (String).
  returns an array list with maps containing keys `:id` `:key` `:new` and `:rev`"
  ([^ArangoCollection coll ^java.util.ArrayList docs]
   (-> (.insertDocuments coll docs)
       bean
       (update :documentsAndErrors
               #(map (fn [x] (-> (bean x)
                                 (dissoc :class)
                                 (set/rename-keys {:id :_id
                                                   :key :_key
                                                   :new :_new
                                                   :rev :_rev}))) %))
       (get :documentsAndErrors)))
  ([^ArangoDatabase db ^String coll-name ^java.util.ArrayList docs]
   (-> db (.collection coll-name) (insert-docs docs)))
  ([^ArangoDB conn ^String db-name ^String coll-name ^java.util.ArrayList docs]
   (-> conn (.db db-name) (insert-docs coll-name docs))))

(defn get-json-by-id ^String
  ([^ArangoDB conn ^String db-name ^String id]
   (get-json-by-id (.db conn db-name) id))
  ([^ArangoDatabase db ^String id]
   (.getDocument db id String)))

(defn get-json-by-key ^String
  ([^ArangoDB conn ^String db-name ^String coll-name ^String key]
   (get-json-by-key (get-collection conn db-name coll-name) key))
  ([^ArangoDatabase db ^String coll-name ^String key]
   (get-json-by-key (get-collection db coll-name) key))
  ([^ArangoCollection coll ^String key]
   (.getDocument coll key String)))

(defn get-vpack-by-id ^VPackSlice
  ([^ArangoDB conn ^String db-name ^String id]
   (get-vpack-by-id (.db conn db-name) id))
  ([^ArangoDatabase db ^String id]
   (.getDocument db id VPackSlice)))

(defn get-vpack-by-key ^VPackSlice
  ([^ArangoDB conn ^String db-name ^String coll-name ^String key]
   (get-vpack-by-key (get-collection conn db-name coll-name) key))
  ([^ArangoDatabase db ^String coll-name ^String key]
   (get-vpack-by-key (get-collection db coll-name) key))
  ([^ArangoCollection coll ^String key]
   (.getDocument coll key VPackSlice)))
