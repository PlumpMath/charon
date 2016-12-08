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

(defn- numbered-line [file]
  (with-open [r (reader file)]
    (doall (map vector (iterate inc 1) (line-seq r)))))

(defn- grep-in-file [pattern file]
  (filter #(re-find pattern (second %)) (numbered-line file)))

(defn- load-index [d]
  (with-open [r (input-stream (str d "/.charon/charon.idx"))]
    (n/thaw-from-in! (DataInputStream. r))))

(defn- query [s d]
  (let [db (load-index d)]
    (map #(get (:file-list db) %)
         (reduce set/intersection
                 (map #(get db %)
                      (index/trigrams s))))))

(defn- print-matches [pattern files]
  (println "count: " (count files))
  (doseq [file files]
    (println file)
    (dorun (map #(printf "%s:%s:%s\n" file (first %) (second %)) (grep-in-file (re-pattern pattern) file)))))

(defn search [opts]
  (let [pattern (:text opts)]
    (print-matches pattern (query pattern (:index opts)))))
