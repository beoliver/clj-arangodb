(ns clj-arangodb.arangodb.databases
  (:import com.arangodb.velocypack.VPackSlice))

;; (map :name (:members (r/reflect db)))

;; (com.arangodb.ArangoDatabase deleteAqlFunction getCurrentlyRunningQueries killQuery collection getVersion getQueryTrackingProperties setQueryCacheProperties explainQuery getSlowQueries grantAccess clearSlowQueries getCollections setCursorInitializer grantAccess revokeAccess cursorInitializer getCollections createCollection resetAccess executeTraversal createCollection setQueryTrackingProperties createGraph com.arangodb.ArangoDatabase graph createAqlFunction getQueryCacheProperties getAqlFunctions access$000 cursor query getInfo getGraphs access$100 transaction getDocument deleteIndex clearQueryCache getDocument createCursor drop getIndex parseQuery reloadRouting createGraph getAccessibleDatabases access$300 updateUserDefaultCollectionAccess access$200)

(defn get-collections
  "returns a `lazySeq` of maps with keys
  `:id`, `:isSystem`, `:isVolatile`, `:name`, `:status`, `:type`, `:waitForSync`"
  [db]
  (map #(-> % bean
            (dissoc :class)
            (update :status str)
            (update :type str))
       (.getCollections db)))

(defn create-collection
  "returns a new `CollectionEntity` - NOT an `ArangoCollection`"
  ([db coll-name]
   (-> db (.createCollection coll-name nil))))

(defn truncate-collection
  ""
  ([db coll-name]
   (-> db (.collection coll-name) .truncate)))

(defn drop-collection
  ""
  ([db coll-name]
   (-> db (.collection coll-name) .drop)))

(defn get-collection
  "returns a new `ArrangoCollection`."
  ([db coll-name]
   (-> db (.collection coll-name))))

(defn get-document-as-vpack
  [db id]
  (.getDocument db id VPackSlice))

(defn get-document-as-json
  [db id]
  (.getDocument db id String))

(defn get-document-as-java-bean
  [db id class]
  (.getDocument db id class))
