(ns mp3-editor.common
  (:require [clojure.java.io :as io]
            [clojure.string :as str])
  (:import (com.mpatric.mp3agic ID3v22Tag
                                ID3v24Tag
                                Mp3File
                                NotSupportedException)
           (java.nio.file Files StandardCopyOption CopyOption)
           (java.io File)))

(def tmp-suffix ".tmp")

(def configuration (atom {:mode             nil
                          :path             nil
                          :file-names       []
                          :testing?         false
                          :recursive?       false
                          :file-name-format [["%ARTIST%" #" - "] ["%ALBUM%" #" - "] ["%TRACK%" #" - "] ["%TITLE%" #"$"]]
                          :artist           nil
                          :album            nil}))

(def file-name-format-map
  {"%ARTIST%" :artist
   "%ALBUM%"  :album
   "%TITLE%"  :title
   "%TRACK%"  :track-number})

(def format-key-re #"%\w+%")

(defn track-number-format
  [^String track-number]
  (when-let [^Long track (some-> (re-find #"\d+" track-number)
                                 (Integer.))]
    (format "%02d" track)))

(def changed-files-count (atom 0))

(def tag-format
  {:artist       identity
   :album        identity
   :title        identity
   :track-number track-number-format})

(def ^:private tag->setter
  {:artist            (fn [^ID3v24Tag tag ^String val] (.setArtist tag val))
   :album             (fn [^ID3v24Tag tag ^String val] (.setAlbum tag val))
   :title             (fn [^ID3v24Tag tag ^String val] (.setTitle tag val))
   :track-number      (fn [^ID3v24Tag tag ^String val] (.setTrack tag val))
   :genre-description (fn [^ID3v24Tag tag ^String val] (.setGenreDescription tag val))})

(defn exit
  [status msg]
  (println msg)
  (System/exit status))

(defn- path->mp3
  [^String path]
  (try
    (Mp3File. path)
    (catch Exception e
      (println "path->mp3 caught exception: " (.getMessage e)))))

(defn read-id3v2
  [^ID3v24Tag mp3-tag]
  (when mp3-tag
    {:artist            (.getArtist mp3-tag)
     :title             (.getTitle mp3-tag)
     :track-number      (.getTrack mp3-tag)
     :album             (.getAlbum mp3-tag)
     :year              (.getYear mp3-tag)
     :genre             (.getGenre mp3-tag)
     :genre-description (.getGenreDescription mp3-tag)
     :comment           (.getComment mp3-tag)
     :composer          (.getComposer mp3-tag)
     :publisher         (.getPublisher mp3-tag)
     :original-artist   (.getOriginalArtist mp3-tag)
     :album-artist      (.getAlbumArtist mp3-tag)
     :copyright         (.getCopyright mp3-tag)
     :lyrics            (.getLyrics mp3-tag)
     :url               (.getUrl mp3-tag)
     :encoder           (.getEncoder mp3-tag)}))

(defn- update-id3v2-version!
  [old-tags ^Mp3File mp3]
  (let [new-tag (ID3v24Tag.)]
    (doseq [[key val] old-tags]
      (when (and val (key tag->setter))
        ((key tag->setter) new-tag val)))
    (.removeId3v2Tag mp3)
    (.setId3v2Tag mp3 new-tag)
    new-tag))

(defn- mp3->id3v2-tag
  ([^Mp3File mp3]
   (mp3->id3v2-tag mp3 false))
  ([^Mp3File mp3 create-if-none?]
   (try
     (cond
       (= (type (.getId3v2Tag mp3)) ID3v22Tag) (update-id3v2-version! (read-id3v2 (.getId3v2Tag mp3)) mp3)
       (.hasId3v2Tag mp3) (.getId3v2Tag mp3)
       create-if-none? (ID3v24Tag.)
       :else nil)
     (catch Exception e
       (println "mp3->id3v2-tag caught exception: " (.getMessage e))))))

(defn file->id3-tag
  [^File file]
  (some-> file
          (.getAbsolutePath)
          (path->mp3)
          (mp3->id3v2-tag)))

(defn write-id3v2!
  "cannot save updated tag in same file so returns new path"
  [^String path tag-info]
  (try
    (let [^Mp3File mp3 (path->mp3 path)
          ^ID3v24Tag id3v2 (mp3->id3v2-tag mp3 true)]
      (doseq [[field-name val] tag-info]
        (if (:testing? @configuration)
          (println tag-info)
          ((field-name tag->setter) id3v2 val)))
      (let [tmp-name (str path tmp-suffix)]
        (.save mp3 tmp-name)
        tmp-name))

    (catch Exception e
      (println "write-id3v2! caught exception: " (.getMessage e)))))

(defn file->new-file
  [^File file ^String new-name]
  (.toFile (.resolveSibling (.toPath file) new-name)))

(defn remove-tmp-suffix-from-files
  [files]
  (doseq [^String path files]
    (let [new-path (subs path 0 (str/index-of path tmp-suffix))]
      (io/delete-file new-path)
      (let [file (io/file path)]
        (.renameTo file (file->new-file file new-path))))))


(defn path->mp3-files
  [^String path]
  (let [file (io/file path)
        file? #(.isFile %)
        mp3? #(str/ends-with? (str/lower-case (str (.toPath %)))
                              ".mp3")
        allowed-location? (if-not (:recursive? @configuration)
                            #(= (.getParent %)
                                (.getAbsolutePath file))
                            identity)]
    (->> (file-seq file)
         (filter #(and (allowed-location? %)
                       (file? %)
                       (mp3? %))))))
