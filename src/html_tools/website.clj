(ns html-tools.website
  (:require
   [html-tools.server :as server]
   [html-tools.api :as html-tools]))


(defn generate-page-file [target file-name page-config]
  (let [file (str target "/" file-name)]
    (println "  *" file-name)
    (html-tools/write-page! page-config file)))


(defn generate-files [target website-config]
  (println "Generating website")
  (println "  target:" target)
  (doall
   (for [[file-name page-config] (get website-config :pages)]
     (generate-page-file target file-name page-config))))


(defn generate [target website-config]
  (case target

    "!http"
    (server/run-http-server website-config)

    (generate-files target website-config)))
