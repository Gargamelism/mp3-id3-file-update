(ns mp3-editor.core
  (:gen-class)
  (:require [clojure.string :as str]
            [clojure.tools.cli :as cli]
            [mp3-editor.common :as common]
            [mp3-editor.file :as file]))

(def ^:private cli-options
  [["-f" "--file" "rename files"
    :default true]
   ["-t" "--tag" "update id3 tag"
    :default false]
   ["-p" "--path PATH" "folder with desired files"]
   ["-n" "--file-names FILE_NAMES" "comma delimeted list of single files to update"]
   ["-F" "--file-name-format FORMAT" "new file name using %ARTIST%, %ALBUM%, %TITLE%, %TRACK. Default is \"%ARTIST% - %ALBUM% - %TITLE% - %TRACK%\""
    :default "%ARTIST% - %ALBUM% - %TITLE% - %TRACK%"]
   ["-d" "--dry-run" "only print the planned changes" :default false]])

(defn- required-args?
  [{:keys [file tag path file-names]}]
  (and (or file tag)
       (or path file-names)))

(defn- process-args
  [{:keys [tag file path file-names dry-run] :as options}]
  (reset! common/configuration (cond-> @common/configuration
                                       tag (assoc :mode :tag)
                                       file (assoc :mode :file)
                                       path (assoc :path path)
                                       file-names (update :file-names #(-> file-names
                                                                           (str/split #",")
                                                                           (concat %)))
                                       dry-run (assoc :testing? true))))

(defn- validate-args
  [args]
  (let [{:keys [options errors summary]} (cli/parse-opts args cli-options)]
    (cond
      (:help options) {:exit-message summary :ok? true}
      errors {:exit-message (str/join "\n" (concat ["ERROR!"] errors))}
      (required-args? options) (process-args options)
      :else {:exit-message summary})))


(defn -main
  [& args]
  (let [{:keys [mode exit-message ok?] :as options} (validate-args args)]
    (if exit-message
      (common/exit (if ok? 0 1) exit-message)
      (case mode
        :file (file/rename-files options)))
    (println "done!")))