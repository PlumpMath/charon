(ns charon.search
  (:require
   [charon.index :as index]
   [clojure.core.reducers :as r]
   [clojure.edn :as edn]
   [clojure.java.io :refer [reader writer file]]
   [clojure.set :as set]
   [clojure.string :as s]
   [iota]
   [rewrite-clj.node :as n]
   [rewrite-clj.parser :as p]
   [rewrite-clj.zip :as z])
  (:import
   (java.io PushbackReader)))

(defn- load-index [d]
  (with-open [r (PushbackReader. (reader (str d "/index.edn")))]
    (edn/read r)))

(defn query [s d]
  (let [index (load-index d)]
    (map #(get (:file-list index) %) (reduce set/intersection
                                             (filter identity
                                                     (map #(get (:trigrams index) %) (index/j (index/trigrams s))))))))

(defn search [opts]
  (println (query (:text opts) (:index opts))))
