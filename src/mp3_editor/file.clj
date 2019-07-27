(ns mp3-editor.file
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [mp3-editor.common :as common])
  (:import (java.io File)
           (java.nio.file Files Path Paths CopyOption StandardCopyOption)))

(def ^:private illegal-name-chars #"[\\/:\"*?<>|]+")
(defn- create-new-name
  [id3-tag]
  (let [required-fields (keep
                          (fn [format-val]
                            (let [name (get common/file-name-format-map format-val)]
                              {:name       name
                               :format-val format-val
                               :val        ((name common/tag-format) (name id3-tag))}))
                          (re-seq #"%\w+%" (:format @common/configuration)))]
    (when (every? :val required-fields)
      (reduce
        (fn [new-name {:keys [format-val name val]}]
          (-> new-name
              (str/replace (re-pattern format-val) val)
              (str/replace illegal-name-chars " ")))
        (:format @common/configuration)
        required-fields))))


(defn- rename-file
  [^File file id3-tag]
  (when-let [new-name (create-new-name id3-tag)]
    (if-not (:testing? @common/configuration)
      (when-not (.renameTo file (.toFile (.resolveSibling (.toPath file) new-name)))
        (println "ERROR! could not rename" (str file) "->" new-name))
      (println (str file) "->" new-name))))



(defn- traverse-folder
  [^String path]
  (reduce
    (fn [additional-folders ^File current-file]
      (let [additional-folders (if (.isDirectory current-file)
                                 (conj additional-folders current-file))]
        (when (str/ends-with? (str/lower-case (str current-file)) ".mp3")
          (rename-file current-file (common/read-id3v2 (str current-file))))
        additional-folders))
    []
    (file-seq (io/file path))))

(defn- rename-files-in-path
  [^String path]
  (loop [folders [path]]
    (when-let [path (first folders)]
      (let [final-folders (concat (rest folders)
                                  (traverse-folder path))]
        (recur final-folders)))))



(defn- rename-files
  [file-names])

(defn rename-files
  [{:keys [file-names path]}]
  (cond
    path (rename-files-in-path path)
    file-names (rename-files file-names)))
