(defproject charon "0.1.0-SNAPSHOT"
  :description "Charon"
  :url "https://github.com/youngker/charon"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/tools.cli "0.3.5"]
                 [org.clojure/tools.logging "0.3.1"]
                 [criterium "0.4.4"]
                 [iota "1.1.3"]
                 [com.taoensso/nippy "2.12.2"]]
  :plugins [[elastic/lein-bin "0.3.6"]]
  :bin {:name "charon"}
  :global-vars {*warn-on-reflection* true}
  :jvm-opts ["-Xmx4g"]
  :aot :all
  :main charon.core)
