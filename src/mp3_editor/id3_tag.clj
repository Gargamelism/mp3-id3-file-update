(ns mp3-editor.id3-tag
  (:require [clojure.java.io :as io]
            [mp3-editor.common :as common]
            [clojure.string :as str])
  (:import (java.io File)))

(defn- file-name->tag
  [^String file-name]
  (let [fields-and-delims (:file-name-format @common/configuration)
        file-name (subs file-name 0 (str/index-of file-name ".mp3"))]
    (-> (reduce
          (fn [{:keys [file-name] :as tag} [field-name delimiter-re]]
            (let [split-name (str/split file-name delimiter-re 2)]
                (assoc tag (get common/file-name-format-map field-name) (first split-name)
                           :file-name (second split-name))))
          {:file-name file-name}
          fields-and-delims)
        (dissoc :file-name nil))))

(defn- tag-info
  [^File file]
  (let [from-file (file-name->tag (.getName file))
        from-conf (cond-> {}
                          (:artist @common/configuration) (assoc :artist (:artist @common/configuration))
                          (:album @common/configuration) (assoc :album (:album @common/configuration)))]
    (merge from-file from-conf)))

(defn- update-multiple-files-tags
  [files]
  (-> (reduce
        (fn [tmp-paths ^File current-file]
          (let [tag-info (tag-info current-file)
                new-path (common/write-id3v2! (.getAbsolutePath current-file) tag-info)]
            (if new-path
              (conj tmp-paths new-path)
              tmp-paths)))
        []
        files)
      (common/remove-tmp-suffix-from-files))
  (reset! common/changed-files-count (count files)))

(defn- update-tags-in-path
  [^String path]
  (let [path (.getAbsolutePath (io/file path))]
    (println "starting from" path (if (:recursive? @common/configuration) "recursively" ""))
    (update-multiple-files-tags (common/path->mp3-files path))))


(defn- update-tags-in-selected-files
  [file-names]
  (update-multiple-files-tags (map #(io/file %) file-names)))

(defn update-tags
  [{:keys [file-names path]}]
  (cond
    path (update-tags-in-path path)
    file-names (update-tags-in-selected-files file-names)))
