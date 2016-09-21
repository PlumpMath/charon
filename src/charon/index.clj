(ns charon.index
  (:require
   [clojure.edn :as edn]
   [clojure.java.io :refer [reader writer file]]
   [clojure.set :as set]
   [clojure.string :as s])
  (:import
   (java.io File)))

(defn file-seq-ignore
  [dir exceptions]
  (let [exceptions (set exceptions)]
    (tree-seq
     (fn [#^File f] (and (.isDirectory f) (not (exceptions (.getName f)))))
     (fn [#^File d] (seq (.listFiles d)))
     dir)))

(defn- ignore? [^File f]
  (and (.isFile f) (pos? (.length f))))

(defn- read-dir [d]
  (let [files (filter ignore? (file-seq-ignore (file d) '(".git" "classes")))]
    (map-indexed vector files)))

(defn trigrams [s]
  (partition 3 1 s))

(defn- get-trigrams [file]
  (println (.getPath ^File file))
  (mapcat #(trigrams (s/triml %)) (s/split-lines (slurp file :encoding "UTF-8"))))

(defn- process [file]
  (map #(hash-map (s/join %) #{(first file)})
       (get-trigrams (second file))))

(defn- write-index [m d]
  (with-open [w (writer (str d "/.charon/index.edn") :encoding "UTF-8")]
    (.write w (pr-str m))))

(defn indexer [opts]
  (let [dir (:index opts)
        files (read-dir dir)
        file-list (zipmap (map first files) (map #(.getPath ^File (second %)) files))
        result (apply merge-with set/union (mapcat process files))]
    (write-index {:file-list file-list :trigrams result} dir)))
