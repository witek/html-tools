(ns html-tools.snippets.browserapp
  (:require
   [html-tools.snippets.preloader :as preloader]
   [cheshire.core :as ceshire]))

(defn js-include [build-name]
  (str "/cljs-out/" (or build-name "prod") "-main.js"))


(defn main-script [app-name browserapp-config]
  (when-not app-name (throw (ex-info "app-name required" {})))
  (let [app-name (.replace app-name "-" "_")
        config-json (ceshire/generate-string {:edn (pr-str browserapp-config)})]
    (str "
var browserapp_config = " config-json ";
" app-name ".main.init(browserapp_config.edn);")))


(defn body []
  [:div {:id "app"}
   preloader/html-code])
