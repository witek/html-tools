(ns html-tools.server
  (:require
   [ring.middleware.reload :as reload]
   [ring.adapter.jetty :as jetty]

   [html-tools.api :as html]))


(def port 9500)


(defn serve-request [website-config req]
  (let [uri (:uri req)
        uri (if (= "/" uri) "/index.html" uri)
        uri (.substring uri 1) ; cause uri always starts with '/'
        page-config (get-in website-config [:pages uri])]
    (if page-config
      {:status 200
       :headers {"Content-Type" "text/html"}
       :body (html/page-html (page-config req))}
      {:status 404})))


(defn run-http-server
  [website-config]
  (println "\nStarting JETTY:      --> " (str "http://localhost:" port "/") "\n")
  (jetty/run-jetty
   (reload/wrap-reload
    (fn [req]
      (serve-request website-config req)))
   {:port port}))


(defn demo-index-page [req]
  {:modules [:bootstrap-cdn] :content "hello world"})

(def demo-website-config
  {:pages {"index.html" demo-index-page}})

(defn -main []
  (run-http-server demo-website-config))
