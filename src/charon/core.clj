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

(def ^{:private true} edn (atom {}))

(defn trigrams [s]
  (partition 3 1 (s/triml s)))

(defn clean [s]
  (map #(keyword (s/replace (s/join %) #" " "_")) s))

(defn parse [f]
  (->> (iota/seq f)
       (r/filter identity)
       (r/map trigrams)
       (r/map clean)
       (into [])))

(defn file? [f]
  (.isFile f))

(defn read-dir [d]
  (filter file? (file-seq (file d))))

(defn index [d]
  (let [files (read-dir d)
        fl (zipmap files (iterate inc 1))
        t (map #(parse (.getCanonicalPath %)) (keys fl))]
    (reset! edn {:file-list fl :trigrams (zipmap (flatten t) (repeat 1))})))
