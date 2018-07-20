(ns clj-arangodb.arangodb.document-options
  (:import com.arangodb.model.DocumentCreateOptions
           com.arangodb.model.DocumentReadOptions
           com.arangodb.model.DocumentReplaceOptions
           com.arangodb.model.DocumentUpdateOptions))

(defn ^DocumentReadOptions map->DocumentReadOptions
  [{:keys [if-none-match if-match catch-exception] :as options}]
  (cond-> (new DocumentReadOptions)
    if-none-match (.ifNoneMatch if-none-match)
    if-match (.ifMatch if-match)
    catch-exception (.catchException catch-exception)))

(defn ^DocumentCreateOptions map->DocumentCreateOptions
  [{:keys [wait-for-sync return-new return-old overwrite silent] :as options}]
  (cond-> (new DocumentCreateOptions)
    wait-for-sync (.waitForSync wait-for-sync)
    return-new (.returnNew return-new)
    return-old (.returnOld return-old)
    overwrite (.overwrite overwrite)
    silent (.silent silent)))

(defn ^DocumentReplaceOptions map->DocumentReplaceOptions
  [{:keys [wait-for-sync ignore-revs if-match return-new return-old silent] :as options}]
  (cond-> (new DocumentReplaceOptions)
    wait-for-sync (.waitForSync wait-for-sync)
    ignore-revs (.ignoreRevs ignore-revs)
    if-match (.ifMatch if-match)
    return-new (.returnNew return-new)
    return-old (.returnOld return-old)
    silent (.silent silent)))

(defn ^DocumentUpdateOptions map->DocumentUpdateOptions
  [{:keys [wait-for-sync ignore-revs if-match return-new return-old silent] :as options}]
  (cond-> (new DocumentUpdateOptions)
    wait-for-sync (.waitForSync wait-for-sync)
    ignore-revs (.ignoreRevs ignore-revs)
    if-match (.ifMatch if-match)
    return-new (.returnNew return-new)
    return-old (.returnOld return-old)
    silent (.silent silent)))
