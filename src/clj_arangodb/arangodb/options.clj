(ns clj-arangodb.arangodb.options
  (:import com.arangodb.model.CollectionCreateOptions
           com.arangodb.entity.CollectionType
           com.arangodb.model.DocumentCreateOptions
           com.arangodb.model.DocumentReadOptions
           com.arangodb.model.DocumentReplaceOptions
           com.arangodb.model.DocumentUpdateOptions
           com.arangodb.model.DocumentDeleteOptions
           com.arangodb.model.DocumentExistsOptions
           com.arangodb.model.DocumentReadOptions
           com.arangodb.model.GraphCreateOptions
           com.arangodb.model.GeoIndexOptions
           com.arangodb.model.VertexCollectionCreateOptions
           com.arangodb.model.VertexCreateOptions
           com.arangodb.model.VertexDeleteOptions
           com.arangodb.model.VertexReplaceOptions
           com.arangodb.model.VertexUpdateOptions
           com.arangodb.model.EdgeCreateOptions
           com.arangodb.model.EdgeDeleteOptions
           com.arangodb.model.EdgeReplaceOptions
           com.arangodb.model.EdgeUpdateOptions
           com.arangodb.model.AqlQueryOptions
           com.arangodb.ArangoDB$Builder))

(def ^:const option-methods
  {;; ArangoDB$Builder stuff
   :password (fn [o v] (.password o v))
   :timeout (fn [o v] (.timeout o v))
   :user (fn [o v] (.user o v))
   :useProtocol (fn [o v] (.useProtocol o v))
   :host (fn [o [host-str port]] (.host o host-str port))
   :sslContext (fn [o v] (-> o (.sslContext v) (.useSsl true)))
   :chunksize (fn [o v] (.chunksize o v))
   :maxConnections (fn [o v] (.maxConnections o v))
   ;;
   :ifNoneMatch (fn [o v] (.ifNoneMatch o v)) ;string
   :ifMatch (fn [o v] (.ifMatch o v)) ;string
   :catchException (fn [o v] (.catchException o v)) ;bool
   :waitForSync (fn [o v] (.waitForSync o v)) ;bool
   :returnNew (fn [o v] (.returnNew o v)) ;bool
   :returnOld (fn [o v] (.returnOld o v)) ;bool
   :overwrite (fn [o v] (.overwrite o v)) ;bool
   :silent (fn [o v] (.silent o v)) ; bool
   :ignoreRevs (fn [o v] (.ignoreRevs o v)) ;bool
   :keepNull (fn [o v] (.keepNull o v)) ;bool
   :orphanCollections (fn [o v] (.orphanCollections o v)) ;string
   :isSmart (fn [o v] (.isSmart o v)) ;bool
   :replicationFactor (fn [o v] (.replicationFactor o v)) ;int
   :numberOfShards (fn [o v] (.numberOfShards o v)) ;int
   :smartGraphAttribute (fn [o v] (.smartGraphAttribute o v)) ;string
   :journalSize (fn [o v] (.journalSize o v)) ;long
   :satellite (fn [o v] (.satellite o v)) ;bool
   :doCompact (fn [o v] (.doCompact o v)) ;bool
   :isVolatile (fn [o v] (.isVolatile o v)) ;bool
   :shardKeys (fn [o v] (.shardKeys o v)) ;string array
   :isSystem (fn [o v] (.doCompact o v)) ;bool
   :type (fn [o v] (.type o v)) ;collection Type
   :indexBuckets (fn [o v] (.indexBuckets o v)) ;int
   :distributeShardsLike (fn [o v] (.distributeShardsLike o v)) ;string
   ;; aql
   :count (fn [o v] (.count o v))
   :ttl (fn [o v] (.ttl o v))
   :batchSize (fn [o v] (.batchSize o v))
   :memoryLimit (fn [o v] (.memoryLimit o v))
   :cache (fn [o v] (.cache o v)) ; bool
   :failOnWarning (fn [o v] (.failOnWarning o v))
   :profile (fn [o v] (.profile o v))
   :maxTransactionSize (fn [o v] (.maxTransactionSize o v))
   :maxWarningCount (fn [o v] (.maxWarningCount o v))
   :intermediateCommitSize (fn [o v] (.intermediateCommitSize o v))
   :satelliteSyncWait (fn [o v] (.satelliteSyncWait o v))
   :skipInaccessibleCollections (fn [o v] (.skipInaccessibleCollections o v))
   :fullCount (fn [o v] (.fullCount o v))
   :maxPlans (fn [o v] (.maxPlans o v))
   :rules (fn [o v] (.rules o v))
   :stream (fn [o v] (.stream o v))
   })

(defn option-builder [object options]
  (reduce (fn [object [k v]]
            ((get option-methods k (constantly object)) object v)) object options))

(defn ^ArangoDB$Builder map->ArangoDB$Builder
  [{:keys [host user password useProtocol sslSontext timeout chunksize maxConnections] :as options}]
  (option-builder (new ArangoDB$Builder) options))

(defn ^CollectionCreateOptions map->CollectionCreateOptions
  [{:keys [journalSize replicationFactor satellite waitForSync doCompact isVolatile
           shardKeys numberOfShards isSystem type indexBuckets distributeShardsLike] :as options}]
  (option-builder (new CollectionCreateOptions) options))

(defn ^AqlQueryOptions map->AqlQueryOptions
  [{:keys [count ttl batchSize memoryLimit cache failOnWarning profile
           maxTransactionSize maxWarningCount intermediateCommitSize
           satelliteSyncWait skipInaccessibleCollections fullCount maxPlans
           rules stream] :as options}]
  (option-builder (new AqlQueryOptions) options))

(defn ^DocumentReadOptions map->DocumentReadOptions
  [{:keys [ifNoneMatch ifMatch catchCxception] :as options}]
  (option-builder (new DocumentReadOptions) options))

(defn ^DocumentCreateOptions map->DocumentCreateOptions
  [{:keys [waitForSync returnNew returnOld overwrite silent] :as options}]
  (option-builder (new DocumentCreateOptions) options))

(defn ^DocumentReplaceOptions map->DocumentReplaceOptions
  [{:keys [waitForSync ignoreRevs ifMatch returnNew returnOld silent] :as options}]
  (option-builder (new DocumentReplaceOptions) options))

(defn ^DocumentUpdateOptions map->DocumentUpdateOptions
  [{:keys [waitForSync ignoreRevs ifMatch returnNew returnOld silent keepNull] :as options}]
  (option-builder (new DocumentUpdateOptions) options))

(defn ^VertexCreateOptions map->VertexCreateOptions
  [{:keys [waitForSync] :as options}]
  (option-builder (new VertexCreateOptions) options))

(defn ^VertexReplaceOptions map->VertexReplaceOptions
  [{:keys [waitForSync ifMatch] :as options}]
  (option-builder (new VertexReplaceOptions) options))

(defn ^VertexUpdateOptions map->VertexUpdateOptions
  [{:keys [waitForSync ifMatch keepNull] :as options}]
  (option-builder (new VertexUpdateOptions) options))

(defn ^VertexDeleteOptions map->VertexDeleteOptions
  [{:keys [waitForSync ifMatch] :as options}]
  (option-builder (new VertexDeleteOptions) options))

(defn ^EdgeCreateOptions map->EdgeCreateOptions
  [{:keys [waitForSync] :as options}]
  (option-builder (new EdgeCreateOptions) options))

(defn ^EdgeReplaceOptions map->EdgeReplaceOptions
  [{:keys [waitForSync ifMatch] :as options}]
  (option-builder (new EdgeReplaceOptions) options))

(defn ^EdgeUpdateOptions map->EdgeUpdateOptions
  [{:keys [waitForSync ifMatch keepNull] :as options}]
  (option-builder (new EdgeUpdateOptions) options))

(defn ^EdgeDeleteOptions map->EdgeDeleteOptions
  [{:keys [waitForSync ifMatch] :as options}]
  (option-builder (new EdgeDeleteOptions) options))

(defn ^GraphCreateOptions map->GraphCreateOptions
  [{:keys [orphanCollections isSmart replicationFactor numberOfShards smartGraphAttribute] :as options}]
  (option-builder (new GraphCreateOptions) options))
