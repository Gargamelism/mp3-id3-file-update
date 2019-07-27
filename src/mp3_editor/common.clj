(ns mp3-editor.common
  (:import (com.mpatric.mp3agic Mp3File)))

(def configuration (atom {:mode       nil
                          :path       nil
                          :file-names []
                          :testing?   true}))

(def file-name-format-map
  {"%ARTIST%" :artist
   "%ALBUM%"  :album
   "%TITLE%"  :title
   "%TRACK%"  :track-number})

(defn track-number-format
  [^String track-number]

  (when-let [^Long track (some-> (re-find #"\d+" track-number)
                                 (Integer.))]
    (format "%02d" track)))

(def tag-format
  {:artist       identity
   :album        identity
   :title        identity
   :track-number track-number-format})

(defn exit
  [status msg]
  (println msg)
  (System/exit status))

(defn read-id3v2
  [^String file-path]
  (try
    (let [mp3 (Mp3File. file-path)]
      (when (.hasId3v2Tag mp3)
        (let [mp3-tag (.getId3v2Tag mp3)]
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
           :encoder           (.getEncoder mp3-tag)})))

    (catch Exception e
      (println "caught exception: " (.getMessage e)))))

