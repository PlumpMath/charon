(ns charon.core
  (:require
   [charon.index :as index]
   [charon.search :as search]
   [clojure.string :as string]
   [clojure.tools.cli :refer [parse-opts]])
  (:gen-class))

(def cli-options
  [["-d" "--index dir" "search,index /path/to/projects"]
   ["-t" "--text text" "search, search text"]])

(defn usage [options-summary]
  (string/join
   \newline
   [""
    "Usage: charon action options"
    "  charon index -d index"
    "  charon search -d index search"
    ""
    "Options:"
    options-summary
    ""
    "Actions:"
    "  index      Creating the index"
    "  search     Searching the text"
    ""
    "See documentation on https://github.com/youngker/charon"]))

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (string/join \newline errors)))

(defn exit [status msg]
  (println msg)
  (System/exit status))

(defn -main [& args]
  (let [{:keys [options arguments errors summary]}
        (parse-opts args cli-options)]
    (cond
      (:help options) (exit 0 (usage summary))
      (not= (count arguments) 1) (exit 1 (usage summary))
      errors (exit 1 (error-msg errors)))
    (case (first arguments)
      "search" (time (search/search options))
      "index" (time (index/indexer options))
      (exit 1 (usage summary)))))
