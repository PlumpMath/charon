(ns charon.core
  (:require
   [clojure.core.reducers :as r]
   [clojure.edn :as edn]
   [clojure.java.io :refer [reader writer file]]
   [clojure.string :as s]
   [iota]
   [rewrite-clj.node :as n]
   [rewrite-clj.parser :as p]
   [rewrite-clj.zip :as z]
   [tesser.core :as t]
   [tesser.math :as m])
  (:import
   (java.io PushbackReader)))

(defn trigrams [s]
  (partition 3 1 (s/triml s)))

(defn clean [s]
  (map #(keyword (s/replace (s/join %) #" " "_")) s))

(defn write [f]
  (with-open [w (writer "index.edn")]
    (let [s (parse f)]
      (.write w (prn-str (into (sorted-map) (frequencies (flatten s))))))))

(defn parse [f]
  (->> (iota/seq f)
       (r/filter identity)
       (r/map trigrams)
       (r/map clean)
       (into [])))

(defn read-dir [d]
  (let [dir (file d)
        files (filter #(.isFile %) (file-seq dir))]
    (doseq [file files]
      (add file))))
