(ns clj-arangodb.arangodb.utils
  (:import com.arangodb.Protocol))

(defn keyword->Protocol
  "returns the interal protocol representation.
  `k` should be one of `:vst`, `:http-vpack`, `:http-json`"
  [k]
  (get {:vst Protocol/VST
        :http-vpack Protocol/HTTP_VPACK
        :http-json Protocol/HTTP_JSON} k))
