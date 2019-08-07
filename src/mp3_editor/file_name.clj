(ns mp3-editor.file-name
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [mp3-editor.common :as common])
  (:import (java.io File)))

(def ^:private illegal-name-chars #"[\\/:\"*?<>|]+")
(defn- create-new-name
  [id3-tag]
  (let [required-fields (keep
                          (fn [format-val]
                            (let [name (get common/file-name-format-map format-val)]
                              {:name       name
                               :format-val format-val
                               :val        ((name common/tag-format) (name id3-tag))}))
                          (re-seq #"%\w+%" (:file-name-format @common/configuration)))]
    (when (every? :val required-fields)
      (reduce
        (fn [new-name {:keys [format-val name val]}]
          (-> new-name
              (str/replace (re-pattern format-val) val)
              (str/replace illegal-name-chars " ")))
        (:file-name-format @common/configuration)
        required-fields))))

(defn- rename-file
  [^File file id3-tag]
  (let [new-name (create-new-name id3-tag)]
    (when (not-empty new-name)
      (swap! common/changed-files-count inc)
      (if-not (:testing? @common/configuration)
        (when-not (.renameTo file (common/file->new-file file new-name))
          (println "ERROR! could not rename" (str file) "->" new-name))
        (println (str file) "->" new-name)))))

(defn- rename-multiple-files
  [files]
  (doseq [^File current-file files]
    (rename-file current-file (common/read-id3v2 (.getAbsolutePath current-file)))))

(defn- rename-files-in-path
  [^String path]
  (let [path (.getAbsolutePath (io/file path))]
    (println "starting from" path (if (:recursive? @common/configuration) "recursively" ""))
    (rename-multiple-files (common/path->mp3-files path))))

(defn- rename-selected-files
  [file-names]
  (rename-multiple-files (map #(io/file %) file-names)))


(defn rename-files
  [{:keys [file-names path]}]
  (cond
    path (rename-files-in-path path)
    file-names (rename-selected-files file-names)))
