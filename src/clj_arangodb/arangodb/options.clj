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
   :use-protocol (fn [o v] (.useProtocol o v))
   :host (fn [o [host-str port]] (.host o host-str port))
   :ssl-context (fn [o v] (-> o (.sslContext v) (.useSsl true)))
   :chunksize (fn [o v] (.chunksize o v))
   :max-connections (fn [o v] (.maxConnections o v))
   ;;
   :if-none-match (fn [o v] (.ifNoneMatch o v)) ;string
   :if-match (fn [o v] (.ifMatch o v)) ;string
   :catch-exception (fn [o v] (.catchException o v)) ;bool
   :wait-for-sync (fn [o v] (.waitForSync o v)) ;bool
   :return-new (fn [o v] (.returnNew o v)) ;bool
   :return-old (fn [o v] (.returnOld o v)) ;bool
   :overwrite (fn [o v] (.overwrite o v)) ;bool
   :silent (fn [o v] (.silent o v)) ; bool
   :ignore-revs (fn [o v] (.ignoreRevs o v)) ;bool
   :keep-null (fn [o v] (.keepNull o v)) ;bool
   :orphan-collections (fn [o v] (.orphanCollections o v)) ;string
   :is-smart (fn [o v] (.isSmart o v)) ;bool
   :replication-factor (fn [o v] (.replicationFactor o v)) ;int
   :number-of-shards (fn [o v] (.numberOfShards o v)) ;int
   :smart-graph-attribute (fn [o v] (.smartGraphAttribute o v)) ;string
   :journal-size (fn [o v] (.journalSize o v)) ;long
   :satellite (fn [o v] (.satellite o v)) ;bool
   :do-compact (fn [o v] (.doCompact o v)) ;bool
   :is-volatile (fn [o v] (.isVolatile o v)) ;bool
   :shard-keys (fn [o v] (.shardKeys o v)) ;string array
   :is-system (fn [o v] (.doCompact o v)) ;bool
   :type (fn [o v] (.type o v)) ;collection Type
   :index-buckets (fn [o v] (.indexBuckets o v)) ;int
   :distribute-shards-like (fn [o v] (.distributeShardsLike o v)) ;string
   ;; aql
   :count (fn [o v] (.count o v))
   :ttl (fn [o v] (.ttl o v))
   :batch-size (fn [o v] (.batchSize o v))
   :memory-limit (fn [o v] (.memoryLimit o v))
   :cache (fn [o v] (.cache o v)) ; bool
   :fail-on-warning (fn [o v] (.failOnWarning o v))
   :profile (fn [o v] (.profile o v))
   :max-transaction-size (fn [o v] (.maxTransactionSize o v))
   :max-warning-count (fn [o v] (.maxWarningCount o v))
   :intermediate-commit-size (fn [o v] (.intermediateCommitSize o v))
   :satellite-sync-wait (fn [o v] (.satelliteSyncWait o v))
   :skip-inaccessible-collections (fn [o v] (.skipInaccessibleCollections o v))
   :full-count (fn [o v] (.fullCount o v))
   :max-plans (fn [o v] (.maxPlans o v))
   :rules (fn [o v] (.rules o v))
   :stream (fn [o v] (.stream o v))
   })

(defn option-builder [object option-keys user-map]
  (let [m (select-keys user-map option-keys)
        option-map (select-keys option-methods option-keys)]
    (reduce (fn [object [k v]]
              ((get option-map k (constantly object)) object v)) object m)))

(defn ^ArangoDB$Builder map->ArangoDB$Builder
  [{:keys [host user password use-protocol ssl-context timeout chunksize max-connections] :as options}]
  (option-builder (new ArangoDB$Builder)
                  [:host :user :password :use-protocol :ssl-context :timeout :chunksize :max-connections]
                  options))

(defn ^CollectionCreateOptions map->CollectionCreateOptions
  [{:keys [journal-size replication-factor satellite wait-for-sync do-compact is-volatile
           shard-keys number-of-shards is-system type index-buckets distribute-shards-like] :as options}]
  (option-builder (new CollectionCreateOptions)
                  [:journal-size :replication-factor :satellite :wait-for-sync :do-compact :is-volatile
                   :shard-keys :number-of-shards :is-system :type :index-buckets :distribute-shards-like]
                  options))

(defn ^AqlQueryOptions map->AqlQueryOptions
  [{:keys [count ttl batch-size memory-limit cache fail-on-warning profile
           max-transaction-size max-warning-count intermediate-commit-size
           satellite-sync-wait skip-inaccessible-collections full-count max-plans
           rules stream] :as options}]
  (option-builder (new AqlQueryOptions)
                  [:count :ttl :batch-size :memory-limit :cache :fail-on-warning :profile
                   :max-transaction-size :max-warning-count :intermediate-commit-size
                   :satellite-sync-wait :skip-inaccessible-collections :full-count :max-plans
                   :rules :stream]
                  options))


(defn ^DocumentReadOptions map->DocumentReadOptions
  [{:keys [if-none-match if-match catch-exception] :as options}]
  (option-builder (new DocumentReadOptions)
                  [:if-none-match :if-match :catch-exception]
                  options))

(defn ^DocumentCreateOptions map->DocumentCreateOptions
  [{:keys [wait-for-sync return-new return-old overwrite silent] :as options}]
  (option-builder (new DocumentCreateOptions)
                  [:wait-for-sync :return-new :return-old :overwrite :silent]
                  options))

(defn ^DocumentReplaceOptions map->DocumentReplaceOptions
  [{:keys [wait-for-sync ignore-revs if-match return-new return-old silent] :as options}]
  (option-builder (new DocumentReplaceOptions)
                  [:wait-for-sync :ignore-revs :return-new :return-old :overwrite :silent]
                  options))

(defn ^DocumentUpdateOptions map->DocumentUpdateOptions
  [{:keys [wait-for-sync ignore-revs if-match return-new return-old silent keep-null] :as options}]
  (option-builder (new DocumentUpdateOptions)
                  [:wait-for-sync :ignore-revs :if-match :return-new :return-old :silent :keep-null]
                  options))

(defn ^VertexCreateOptions map->VertexCreateOptions
  [{:keys [wait-for-sync] :as options}]
  (option-builder (new VertexCreateOptions)
                  [:wait-for-sync]
                  options))

(defn ^VertexReplaceOptions map->VertexReplaceOptions
  [{:keys [wait-for-sync if-match] :as options}]
  (option-builder (new VertexReplaceOptions)
                  [:wait-for-sync :if-match]
                  options))

(defn ^VertexUpdateOptions map->VertexUpdateOptions
  [{:keys [wait-for-sync if-match keep-null] :as options}]
  (option-builder (new VertexUpdateOptions)
                  [:wait-for-sync :if-match :keep-null]
                  options))

(defn ^VertexDeleteOptions map->VertexDeleteOptions
  [{:keys [wait-for-sync if-match] :as options}]
  (option-builder (new VertexDeleteOptions)
                  [:wait-for-sync :if-match]
                  options))

(defn ^EdgeCreateOptions map->EdgeCreateOptions
  [{:keys [wait-for-sync] :as options}]
  (option-builder (new EdgeCreateOptions)
                  [:wait-for-sync]
                  options))

(defn ^EdgeReplaceOptions map->EdgeReplaceOptions
  [{:keys [wait-for-sync if-match] :as options}]
  (option-builder (new EdgeReplaceOptions)
                  [:wait-for-sync :if-match]
                  options))

(defn ^EdgeUpdateOptions map->EdgeUpdateOptions
  [{:keys [wait-for-sync if-match keep-null] :as options}]
  (option-builder (new EdgeUpdateOptions)
                  [:wait-for-sync :if-match :keep-null]
                  options))

(defn ^EdgeDeleteOptions map->EdgeDeleteOptions
  [{:keys [wait-for-sync if-match] :as options}]
  (option-builder (new EdgeDeleteOptions)
                  [:wait-for-sync :if-match]
                  options))

(defn ^GraphCreateOptions map->GraphCreateOptions
  [{:keys [orphan-collections is-smart replication-factor number-of-shards smart-graph-attribute] :as options}]
  (option-builder (new GraphCreateOptions)
                  [:orphan-collections :is-smart :replication-factor :number-of-shards :smart-graph-attribute]
                  options))
