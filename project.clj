(defproject task-manager "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :repositories [["clojars" {:url "https://repo.clojars.org/"}]]
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [ring "1.9.5"]
                 [compojure "1.6.3"]
                 [cheshire "5.11.0"]
                 [ring/ring-mock "0.4.0"]
                 [metosin/spec-tools "0.10.7"]]
  :main ^:skip-aot task-manager.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})