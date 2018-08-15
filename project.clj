(defproject beoliver/clj-arangodb "0.0.8"
  :description "A Clojure wrapper for ArangoDB"
  :url "https://github.com/beoliver/clj-arangodb"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :source-paths ["src/clojure"]
  :java-source-paths ["src/java"]
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [com.arangodb/arangodb-java-driver "4.7.0"]
                 [org.slf4j/slf4j-simple "1.7.25"]])
