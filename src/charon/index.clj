(ns charon.index
  (:require
   [clojure.edn :as edn]
   [clojure.core.reducers :as r]
   [clojure.java.io :refer [reader writer file output-stream]]
   [clojure.set :as set]
   [clojure.string :as s]
   [clojure.tools.logging :as log]
   [criterium.core :as c]
   [iota]
   [taoensso.nippy :as n])
  (:import
   (java.io File DataOutputStream)))

(defn- file-seq-ignore
  [dir exceptions]
  (let [exceptions (set exceptions)]
    (tree-seq
     (fn [#^File f] (and (.isDirectory f) (not (exceptions (.getName f)))))
     (fn [#^File d] (seq (.listFiles d)))
     dir)))

(defn- read-dir [d]
  (let [files (filter (comp not #(.isDirectory ^File %))
                      (file-seq-ignore (file d) '(".git" ".charon")))]
    (map-indexed vector files)))

(defn trigrams [s]
  (map s/join (partition 3 1 s)))

(defn- utf8? [c]
  (let [ci (int c)
        c1 (bit-and (bit-shift-right ci 8) 0xff)
        c2 (bit-and ci 0xff)]
    (cond
      (< c1 0x80) (or (< c2 0x80) (and (<= 0xc0 c2) (< c2 0xf8)))
      (< c1 0xc0) (< c2 0xf8)
      (< c1 0xf8) (and (<= 0x80 c2) (< c2 0xc0))
      :else false)))

(defn- write-index [m d]
  (with-open [w (writer (str d "/.charon/index.edn") :encoding "UTF-8")]
    (.write w (pr-str m))))

(def trigrams-reducer
  (comp
   (r/mapcat trigrams)
   (r/map s/triml)
   (r/filter identity)))

(defn index-file
  ([] {})
  ([index f]
   (let [file (.getPath ^File (second f))]
     (println file)
     (try
       (r/fold
        (fn
          ([] (assoc-in index [:file-list (first f)] file))
          ([& m]
           (apply merge-with into m)))
        (fn
          [idx term]
          (if (every? utf8? term)
            (assoc idx term (conj (get idx term #{}) (first f)))
            (reduced index)))
        (trigrams-reducer (iota/seq file)))
       (catch java.io.FileNotFoundException e
         (log/warn (format "%s is not found." file))
         index)
       (catch java.lang.ArrayIndexOutOfBoundsException e
         (log/warn (format "%s is empty." file))
         index)))))

(defn make-index [files]
  (with-open [w (output-stream ".charon/charon.idx")]
    (n/freeze-to-out! (DataOutputStream. w) (r/fold merge index-file files))))

(defn benchmark [f N times]
  (let [nums (vec (range N))
        start (java.lang.System/currentTimeMillis)]
    (dotimes [n times]
      (f nums))
    (- (java.lang.System/currentTimeMillis) start)))

(defn indexer [opts]
  (let [dir (:index opts)
        trigrams (make-index (read-dir dir))]
    (println "Done.")))
