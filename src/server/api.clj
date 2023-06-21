(ns server.api
  (:require
    [server.marketdata :as marketdata]
    [server.positions :as positions]
    [server.bank :as bank]
    [server.vault :as vault]
    [server.cellar :as cellar]

    [org.httpkit.server :as server]
    [ring.middleware.cors :refer [wrap-cors]]
    [ring.middleware.gzip :as gzip]
    [ring.middleware.defaults :refer :all]
    [compojure.core :refer :all]
    [compojure.route :as route]
    )
  )

;----------------------------------------------------------------------------------------------------------------------
(defn daily-run! []
(try
  (println "Refreshing fx...")             (marketdata/refresh-fx-data!)
  (println "Refreshing commodities...")    (marketdata/refresh-commodities-data!)
  (println "Refreshing positions...")      (positions/refresh-positions-data!)
  (println "Refreshing bank...")           (bank/refresh-bank-data!)
  (println "Refreshing vault...")          (vault/refresh-vault-data!)
  (println "Refreshing cellar...")         (cellar/refresh-cellar-data!)
  ))
;--------------------------------------------------------SERVER SET UP-------------------------------------------------
(def site-default
  "A default configuration for a browser-accessible website, based on current best practice."
  {:params    {:urlencoded true
               :multipart  true
               :nested     true
               :keywordize true}
   :cookies   true
   :session   {:flash true
               :cookie-attrs {:http-only true, :same-site :strict}}
   :security  {:anti-forgery   true
               :frame-options  :sameorigin
               :content-type-options :nosniff}
   :static    {:resources "public"}
   :responses {:not-modified-responses true
               :absolute-redirects     false
               :content-types          true
               :default-charset        "utf-8"}})

(defn print-request [req]
  {:status  200
   :headers {"Content-Type" "application/json"}
   :body    req})

(defroutes app-routes
           (GET "/positions-summary"                            [] positions/positions-summary)
           (GET "/bank-summary"                                 [] bank/bank-summary)
           (GET "/vault-summary"                                [] vault/vault-summary)
           (GET "/cellar-summary"                               [] cellar/cellar-summary)
           ;(POST "/quant-model-save-new-bond"       [] post-quant-model-save-new-bond!)
           (route/not-found print-request)
           )

(def jasmine-app
  (-> #'app-routes
      ;(rlogger/wrap-log-request-params  {:transform-fn #(assoc % :level :info)})
      (wrap-defaults (assoc-in site-default [:security :anti-forgery] false))
      (wrap-cors :access-control-allow-origin [#".*"]
                 :access-control-allow-methods [:get :post]
                 :access-control-allow-credentials true)
      ;(wrap-json-response)
      (gzip/wrap-gzip)
      ))

(defonce jasmine-server (atom nil))

(defn start-web-server! [port]
  (when-not (nil? @jasmine-server)
    (@jasmine-server)                                       ;this is a function that stops the server
    (reset! jasmine-server nil))
  (reset! jasmine-server (server/run-server jasmine-app {:port port :max-body 32000000})) ;32MB, default is 8MB, for quant score upload
  (println (str "Running webserver at http:/127.0.0.1:" port "/")))

;----------------------------------------------------------------------------------------------------------------------