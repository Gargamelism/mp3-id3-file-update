(ns mp3-editor.core
  (:gen-class)
  (:require [clojure.string :as str]
            [clojure.tools.cli :as cli]
            [mp3-editor.common :as common]
            [mp3-editor.file-name :as file-name]
            [mp3-editor.id3-tag :as id3-tag]))

(def ^:private cli-options
  [["-f" "--file-name" "rename files"
    :default false]
   ["-t" "--tag" "update id3 tag"
    :default false]
   ["-p" "--path PATH" "folder with desired files"]
   ["-n" "--file-names FILE_NAMES" "comma delimeted list of single files to update"]
   ["-F" "--file-name-format FORMAT" "format for file renaming or info retrieval from files (to fill id3) %ARTIST%, %ALBUM%, %TITLE%, %TRACK. Default is \"%ARTIST% - %ALBUM% - %TRACK% - %TITLE%\""]
   ["-a" "--artist ARTIST" "track's artist, shouldn't be used recursively unless root is band's dir - can be used with format"]
   ["-A" "--album ALBUM" "track's album, shouldn't be used recursively - can be used with format"]
   ["-d" "--dry-run" "only print the planned changes" :default false]
   ["-r" "--recursive" "change files in all subfolders" :default false]])

(defn- required-args?
  [{:keys [file-name tag path file-names]}]
  (and (or file-name tag)
       (or path file-names)))

(defn- format-parts->format-pairs
  [format-parts]
  (let [[format-pairs last-element]
        (reduce
          (fn [[parts last-part] current-part]
            (cond
              (and (empty? parts) (not last-part) (not (re-find common/format-key-re current-part))) [{"%BEGIN%" (re-pattern current-part)} nil]
              last-part [(conj parts [last-part (re-pattern current-part)]) nil]
              :else [parts current-part]))
          [[] nil]
          format-parts)]
    (if last-element
      (conj format-pairs [last-element #"$"])
      format-pairs)))

(let [file-name-parts-re #"(.*?)(%\w+%)(.*?)"]
  (defn- file-name-format->format-parts
    [^String file-name-format]
    (->> (re-seq file-name-parts-re file-name-format)
         (map rest)
         flatten
         (filter not-empty)
         (format-parts->format-pairs))))

(defn- process-args
  [{:keys [tag file-name path file-names dry-run recursive file-name-format artist album] :as options}]
  (let [conf (cond-> @common/configuration
                     tag (assoc :mode :tag)
                     file-name (assoc :mode :file)
                     path (assoc :path path)
                     file-names (update :file-names #(-> file-names
                                                         (str/split #",")
                                                         (concat %)))
                     dry-run (assoc :testing? true)
                     recursive (assoc :recursive? true)
                     file-name-format (assoc :file-name-format (file-name-format->format-parts file-name-format))
                     artist (assoc :artist artist)
                     album (assoc :album album))]
    (reset! common/configuration conf)))

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
        :file (file-name/rename-files options)
        :tag (id3-tag/update-tags options)))
    (println "updated" @common/changed-files-count "files\ndone!")))
