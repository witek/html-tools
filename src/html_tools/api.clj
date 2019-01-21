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
  ;; (println "Writing" file)
  (spit file (page-html (page-config nil))))


(defn error [ex]
  [:div
   {:style (style {:background-color "#ffaaaa"
                   :border "1px solid red"
                   :margin "10px"
                   :padding "10px"
                   :white-space :pre-wrap
                   :font-family :monospace})}
   [:div "Error!"]
   [:div
    (escape (pr-str ex))]])


(defn error-boundary-fn [component-fn & args]
  (try
     (apply component-fn args)
     (catch Exception ex
       ;;(.printStackTrace ex)
       (error ex))))


(defmacro error-boundary [body]
  `(error-boundary-fn
    (fn [] ~body)))
