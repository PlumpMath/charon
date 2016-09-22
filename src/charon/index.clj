(ns charon.index
  (:require
   [clojure.edn :as edn]
   [clojure.java.io :refer [reader writer file]]
   [clojure.set :as set]
   [clojure.string :as s])
  (:import
   (java.io File)))

(defn- file-seq-ignore
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

(defn- utf8? [c1 c2]
  (cond
    (< c1 0x80) (or (< c2 0x80) (and (<= 0xc0 c2) (< c2 0xf8)))
    (< c1 0xc0) (< c2 0xf8)
    (< c1 0xf8) (and (<= 0x80 c2) (< c2 0xc0))
    :else false))

(defn- check-trigram [s]
  (if (some false? (map #(utf8? (bit-and (bit-shift-right (int %) 8) 0xff)
                                (bit-and (int %) 0xff)) s))
    nil
    (s/join s)))

(defn- read-file [#^File file]
  (println (.getPath file))
  (apply concat (reduce (fn [v l]
                          (let [t (trigrams (s/triml l))
                                s (map #(check-trigram %) t)]
                            (if (some nil? s)
                              (reduced nil)
                              (do () (conj v s)))))
                        []
                        (s/split-lines (slurp file :encoding "UTF-8")))))

(defn- get-trigrams [file]
  (zipmap (read-file (second file)) (repeat #{(first file)})))

(defn- write-index [m d]
  (with-open [w (writer (str d "/.charon/index.edn") :encoding "UTF-8")]
    (.write w (pr-str m))))

(defn indexer [opts]
  (let [dir (:index opts)
        files (read-dir dir)
        file-list (zipmap (map first files) (map #(.getPath ^File (second %)) files))
        trigrams (apply merge-with set/union (pmap get-trigrams files))]
    (time (write-index {:file-list file-list :trigrams trigrams} dir))
    (shutdown-agents)))
