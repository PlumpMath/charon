(defproject charon "0.1.0-SNAPSHOT"
  :description "Charon"
  :url "https://github.com/youngker/charon"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [tesser.core "1.0.1"]
                 [tesser.math "1.0.1"]
                 [rewrite-clj "0.5.2"]
                 [org.clojure/tools.cli "0.3.5"]
                 [iota "1.1.3"]]
  :plugins [[elastic/lein-bin "0.3.6"]]
  :bin {:name "charon"}
  :global-vars {*warn-on-reflection* true}
  :jvm-opts ["-Xmx2g"]
  :aot :all
  :main charon.core)
