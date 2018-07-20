(ns clj-arangodb.arangodb.utils
  (:require [clj-arangodb.velocypack.core :as vpack])
  (:import com.arangodb.Protocol))

(defn keyword->Protocol
  "returns the interal protocol representation.
  `k` should be one of `:vst`, `:http-vpack`, `:http-json`"
  [k]
  (get {:vst Protocol/VST
        :http-vpack Protocol/HTTP_VPACK
        :http-json Protocol/HTTP_JSON} k))

(defn MultiDocumentEntity->map [o]
  (-> (bean o)
      (update :documents #(map bean %))
      (update :errors #(map bean %))
      (update :documentsAndErrors #(map bean %))))

(defn maybe-vpack [doc]
  (if (map? doc) (vpack/pack doc) doc))
