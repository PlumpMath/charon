(ns charon.search
  (:require
   [charon.index :as index]
   [clojure.edn :as edn]
   [clojure.java.io :refer [reader writer file input-stream]]
   [clojure.set :as set]
   [clojure.string :as s]
   [taoensso.nippy :as n]
   [clojure.core.reducers :as r])
  (:import
   (java.io PushbackReader DataInputStream)))

(defn- load-index [d]
  (with-open [r (input-stream ".charon/charon.idx")]
    (n/thaw-from-in! (DataInputStream. r))))

(defn query [s d]
  (let [db (load-index d)]
    (map #(get (:file-list db) %)
         (reduce set/intersection
                 (filter identity
                         (map #(get db %)
                              (index/trigrams s)))))))

(defn search [opts]
  (println (query (:text opts) (:index opts))))
