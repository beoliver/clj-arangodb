(ns clj-arangodb.arangodb.oasis
  (:import
   [java.io ByteArrayInputStream]
   [javax.net.ssl SSLContext TrustManagerFactory]
   [java.security KeyStore]
   [java.security.cert CertificateException CertificateFactory X509Certificate]
   [java.util Base64]))

;; ====  SSL  ==================================================================

(defn initialize-ssl-context
  [^String certificate]
  (let [is (new ByteArrayInputStream (.decode (Base64/getDecoder) certificate))
        cf (CertificateFactory/getInstance "X.509")
        caCert (.generateCertificate cf is)
        tmf (TrustManagerFactory/getInstance (TrustManagerFactory/getDefaultAlgorithm))
        ks (KeyStore/getInstance (KeyStore/getDefaultType))
        ssl-context (SSLContext/getInstance "TLS")]
    (.load ks nil)
    (.setCertificateEntry ks "caCert" caCert)
    (.init tmf ks)
    (.init ssl-context nil (.getTrustManagers tmf) nil)))
