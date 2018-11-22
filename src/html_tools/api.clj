(ns html-tools.api
  (:require
   [hiccup.page :as hiccup]
   [hiccup.util :as util]
   [html-tools.css :as css]
   [html-tools.htmlgen :as htmlgen]))


(def escape util/escape-html)


(def css css/css)


(def style css/style)


(def page-html htmlgen/page-html)


(defn write-page!
  [page-config file]
  (println "Writing" file)
  (spit file (page-html page-config)))


