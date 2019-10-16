(ns html-tools.htmlgen
  (:require
   [hiccup.page :as hiccup]
   [hiccup.util :as util]
   [html-tools.css :as css]
   [html-tools.snippets.preloader :as preloader]
   [html-tools.snippets.browserapp :as browserapp]
   [html-tools.snippets.google-analytics :as google-analytics]))


(def modules
  {:bootstrap-cdn
   {:css-includes ["https://stackpath.bootstrapcdn.com/bootstrap/4.1.3/css/bootstrap.min.css"]
    :js-includes ["https://code.jquery.com/jquery-3.3.1.slim.min.js"
                  "https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.14.3/umd/popper.min.js"
                  "https://stackpath.bootstrapcdn.com/bootstrap/4.1.3/js/bootstrap.min.js"]}

   :page-reload
   {:js-includes ["https://github.com/witek/page-reload/releases/download/v1.0.1/page-reload.js"]
    :js-scripts ["page_reload.api.watch();"]}

   :manifest-json
   {:links [{:rel "manifest" :href "/manifest.json"}]}

   :browserapp
   (fn [request
        {:as config
         :keys [js-build-name
                browserapp-name
                browserapp-config-f]}]
     {:css-codes [preloader/css-code]
      :js-includes [(browserapp/js-include js-build-name)]
      :js-scripts [(browserapp/main-script
                    browserapp-name
                    (browserapp-config-f request))]
      :body-contents-before [(browserapp/body)]})

   :google-analytics
   (fn [request
        {:as config
         :keys [google-analytics-tracking-id]}]
     {:head-contents (google-analytics/html-head-components google-analytics-tracking-id)})})


(def default-config
  {:charset "utf-8"
   :viewport "width=device-width, initial-scale=1, shrink-to-fit=no"
   :title "generated by html-tools"
   :css-includes []
   :css-inlines []
   :css-maps []
   :google-fonts []
   :js-includes []
   :js-inlines []
   :js-scripts []})


(defn page-head
  [config]
  [:head
   "\n"
   [:meta {:charset (:charset config)}]
   "\n"
   [:meta {:name "viewport"
           :content (:viewport config)}]

   "\n"
   (for [link (:links config)]
     [:link link])

   "\n"
   (for [content (:head-contents config)]
     content)

   "\n"
   (for [uri (:css-includes config)]
     (hiccup/include-css uri))

   (for [font (:google-fonts config)]
     [:link
      {:href (str "https://fonts.googleapis.com/css?family=" font)
       :rel "stylesheet"
       :type "text/css"}])
     ;;(hiccup/include-css (str "https://fonts.googleapis.com/css?family=" font)))

   (for [css-inline (:css-inlines config)]
     [:style (slurp css-inline)])

   (for [css-code (:css-codes config)]
     [:style css-code])

   (for [css-map (:css-maps config)]
     [:style (css/css css-map)])

   (if-let [css-map (:css config)]
     [:style (css/css css-map)])

   "\n"
   [:title (util/escape-html (:title config))]

   "\n"])

(defn- js-include->tag-attrs [js-include]
  (if (string? js-include)
    {:src js-include}
    js-include))

(defn page-body
  [config]
  (-> [:body]

      (conj "\n")

      (into
       (for [content (:body-contents-before config)]
         content))

      (conj "\n")

      (into (:content config))

      (conj "\n")

      (into
       (for [js-include (:js-includes config)]
         [:script (js-include->tag-attrs js-include)]))

      (conj "\n")

      (into
        (for [js-inline (:js-inlines config)]
          [:script (slurp js-inline)]))

      (conj "\n")

      (into (for [script (:js-scripts config)] [:script script]))

      (conj "\n")

      (conj [:script (:script config)])

      (conj "\n")))


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
  [request config module-key]
  (let [content (get modules module-key)]
    (if (fn? content)
      (content request config)
      content)))


(defn- process-config
  [request config]
  (let [configs (into [] (map (partial module->content request config) (:modules config)))
        configs (conj configs config)]
    (reduce
     (fn [ret config]
       (merge-config ret config))
     default-config
     configs)))


(defn page-html
  [request config]
  (let [config (process-config request config)]
    (hiccup/html5 (page-head config) (page-body config))))
