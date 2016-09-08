(ns charon.index
  (:require
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
   (java.io PushbackReader File)))

(defn trigrams [s]
  (partition 3 1 (s/triml s)))

(defn- valid-char? [c]
  (let [ch (int (.charValue c))]
    (or (#{ \newline \tab \return } c)
        (<= 0x20 ch 0x3F)
        (<= 0x40 ch 0x7D)
        (#{\à \á \â \ã \ç \è \é \ê \í
           \À \Á \Â \Ã \Ç \È \É \Ê \Í
           \õ \ó \ô \ú \û \ü
           \Õ \Ó \Ô \Ú \Û } c))))

(defn- valid-check [s]
  (when (every? true? (map valid-char? s))
    (s/join s)))

(defn j [s]
  (filter identity (map valid-check s)))

(defn- parse [f n]
  (zipmap
   (flatten (->> (iota/seq f)
                 (r/filter identity)
                 (r/map trigrams)
                 (r/map j)
                 (into ())))
   (repeatedly #(set (list n)))))

(defn file-seq-ignore
  "A tree seq on java.io.Files that ignores directories in the exceptions list.
  cf. clojure.core/file-seq"
  [dir exceptions]
  (let [exceptions (set exceptions)]
    (tree-seq
     (fn [#^java.io.File f] (and (.isDirectory f) (not (exceptions (.getName f)))))
     (fn [#^java.io.File d] (seq (.listFiles d)))
     dir)))

(defn- ignore? [^File f]
  (and (.isFile f) (pos? (.length f))))

(defn- read-dir [d]
  (map #(.getCanonicalPath ^File %) (filter ignore? (file-seq-ignore (file d)
                                                                     '(".git"
                                                                       "classes")))))

(defn- get-trigrams [fl]
  (reduce (partial merge-with into) (pmap #(parse %1 %2) (vals fl) (keys fl))))

(defn- write-index [m d]
  (with-open [w (writer (str d "/.charon/index.edn") :encoding "UTF-8")]
    (.write w (pr-str m))))

(defn indexer [opts]
  (let [files (time (read-dir (:index opts)))
        fl (time (zipmap (iterate inc 1) files))
        t (time (get-trigrams fl))]
    (time (write-index {:file-list fl :trigrams t} (:index opts)))))


