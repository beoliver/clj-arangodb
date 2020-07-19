(ns clj-arangodb.arangodb.oasis
  (:require
   [clj-arangodb.arangodb.core :as ar]
   [clj-arangodb.arangodb.databases :as d])
  (:import
   [java.io ByteArrayInputStream]
   [javax.net.ssl SSLContext TrustManagerFactory]
   [java.security KeyStore]
   [java.security.cert CertificateException CertificateFactory X509Certificate]
   [java.util Base64]))

;; ====  SSL  ==================================================================

(def arango-certificate (System/getenv "ARANGO_CERTIFICATE"))

(def is (new ByteArrayInputStream (.decode (Base64/getDecoder) arango-certificate)))
(def cf (CertificateFactory/getInstance "X.509"))
(def caCert (.generateCertificate cf is))
(def tmf (TrustManagerFactory/getInstance (TrustManagerFactory/getDefaultAlgorithm)))
(def ks (KeyStore/getInstance (KeyStore/getDefaultType)))

(.load ks nil)
(.setCertificateEntry ks "caCert" caCert)
(.init tmf ks)
(def ssl-context (SSLContext/getInstance "TLS"))
(.init ssl-context nil (.getTrustManagers tmf) nil)

;; ====  CONNECTION  ===========================================================

(def arango-host (System/getenv "ARANGO_HOST"))
(def arango-port (Integer. (System/getenv "ARANGO_PORT")))
(def arango-user (System/getenv "ARANGO_USER"))
(def arango-password (System/getenv "ARANGO_PASSWORD"))

(def conn (ar/connect {:user arango-user
                       :password arango-password
                       :host [arango-host arango-port]
                       :useSsl true
                       :sslContext ssl-context}))

(def db (ar/get-database conn "<db-name>"))
(def example-coll (d/get-collection db "<example-doc-coll>"))
