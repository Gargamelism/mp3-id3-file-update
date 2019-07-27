(defproject mp3-editor "0.0.1"
  :description "script to automate mp3 name editing and tag editing"
  :url "https://github.com/Gargamelism"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/tools.cli "0.4.2"]
                 [com.mpatric/mp3agic "0.9.1"]]
  :main mp3-editor.core
  :aot [mp3-editor.core])