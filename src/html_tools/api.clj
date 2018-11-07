(ns html-tools.api
  (:require
   [hiccup.page :as hiccup]
   [hiccup.util :as util]))

(def templates
  {:bootstrap-cdn {:css-includes ["https://stackpath.bootstrapcdn.com/bootstrap/4.1.3/css/bootstrap.min.css"]
                   :js-includes ["https://code.jquery.com/jquery-3.3.1.slim.min.js"
                                 "https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.14.3/umd/popper.min.js"
                                 "https://stackpath.bootstrapcdn.com/bootstrap/4.1.3/js/bootstrap.min.js"]}})


(def escape util/escape-html)


(defn- style-val
  [v]
  (cond
    (keyword? v) (name v)
    :else (str v)))

(defn style
  [css-prop-map]
  (reduce
   (fn [s [k v]]
     (str s (name k) ": " (style-val v) "; "))
   ""
   css-prop-map))


(defn css
  [css]
  (reduce
   (fn [s [k v]]
     (str s (style-val k) " {" (style v) "}\n"))
   "\n"
   css))

(defn page-html
  [config]
  (hiccup/html5
   [:head
    [:meta {:charset "utf-8"}]
    [:meta {:name "viewport"
            :content "width=device-width, initial-scale=1, shrink-to-fit=no"}]
    (for [uri (:css-includes config)]
      (hiccup/include-css uri))
    (if-let [css-map (:css config)]
      [:style (css css-map)])
    (if-let [title (:title config)]
      [:title (escape title)])]
   (-> [:body]
       (into (:content config))
       (into (map hiccup/include-js (:js-includes config)))
       (conj [:script (:script config)]))))


(defn write-page!
  [page-config file]
  (println "Writing" file)
  (spit file (page-html page-config)))
