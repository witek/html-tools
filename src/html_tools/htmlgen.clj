(ns html-tools.htmlgen
  (:require
   [hiccup.page :as hiccup]
   [hiccup.util :as util]
   [html-tools.css :as css]))


(def modules
  {:bootstrap-cdn {:css-includes ["https://stackpath.bootstrapcdn.com/bootstrap/4.1.3/css/bootstrap.min.css"]
                   :js-includes ["https://code.jquery.com/jquery-3.3.1.slim.min.js"
                                 "https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.14.3/umd/popper.min.js"
                                 "https://stackpath.bootstrapcdn.com/bootstrap/4.1.3/js/bootstrap.min.js"]}
   :page-reload {:js-includes ["https://github.com/witek/page-reload/releases/download/v1.0.1/page-reload.js"]
                 :js-scripts ["page_reload.api.watch();"]}})


(def default-config
  {:charset "utf-8"
   :viewport "width=device-width, initial-scale=1, shrink-to-fit=no"
   :title "generated by html-tools"
   :css-includes []
   :css-maps []
   :google-fonts []
   :js-includes []
   :js-scripts []})


(defn page-head
  [config]
  [:head
   [:meta {:charset (:charset config)}]
   [:meta {:name "viewport"
           :content (:viewport config)}]

   (for [uri (:css-includes config)]
     (hiccup/include-css uri))

   (for [font (:google-fonts config)]
     (hiccup/include-css (str "https://fonts.googleapis.com/css?family=" font)))

   (for [css-map (:css-maps config)]
     [:style (css/css css-map)])

   (if-let [css-map (:css config)]
     [:style (css/css css-map)])

   (if-let [title (:title config)]
     [:title (util/escape-html title)])])


(defn page-body
  [config]
  (-> [:body]
      (into (:content config))

      (into (map hiccup/include-js (:js-includes config)))

      (into (for [script (:js-scripts config)] [:script script]))

      (conj [:script (:script config)])))


(defn- deep-merge [v & vs]
  (letfn [(rec-merge [v1 v2]
            (if (and (map? v1) (map? v2))
              (merge-with deep-merge v1 v2)
              v2))]
    (when (some identity vs)
      (reduce #(rec-merge %1 %2) v vs))))


(defn- merge-config
  [target source]
  (reduce
   (fn [target [k source-v]]
     (if-let [target-v (get target k)]
       (cond
         (map? target-v) (assoc target k (merge target-v source-v))
         (seq? target-v) (assoc target k (into target-v source-v))
         :else (assoc target k source-v))
       (assoc target k source-v)))
   target
   source))


(defn- module->content
  [module-key]
  (get modules module-key))


(defn- process-config
  [config]
  (let [configs (into [] (map module->content (:modules config)))
        configs (conj configs config)]
    (reduce
     (fn [ret config]
       (merge-config ret config))
     default-config
     configs)))


(defn page-html
  [config]
  (let [config (process-config config)]
    (hiccup/html5 (page-head config) (page-body config))))