(ns html-tools.server
  (:require
   [ring.middleware.reload :as reload]
   [ring.adapter.jetty :as jetty]

   [html-tools.api :as html]))


(def port 9500)


(defn serve-request [page-config req]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (html/page-html page-config)})


(defn run-http-server
  [page-config]
  (println "\nStarting JETTY:      --> " (str "http://localhost:" port "/") "\n")
  (jetty/run-jetty
   (reload/wrap-reload
    (fn [req]
      (serve-request page-config req)))
   {:port port}))


(defn -main []
  (run-http-server (merge (:bootstrap-cdn html/templates) {:content "hello world"})))
