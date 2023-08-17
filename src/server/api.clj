(ns server.api
  (:require
    [server.marketdata :as marketdata]
    [server.positions :as positions]
    [server.bank :as bank]
    [server.vault :as vault]
    [server.cellar :as cellar]
    ;[common.api :as capi :refer [->transit-string pb gpj gpt gpx gpp gptds]]
    [org.httpkit.server :as server]
    [ring.middleware.cors :refer [wrap-cors]]
    [ring.middleware.gzip :as gzip]
    [ring.middleware.defaults :refer :all]

    [compojure.core :refer :all]
    [compojure.route :as route]

    [jsonista.core :as jsonista]
    [cognitect.transit :as transit]
    )
  (:import (java.io ByteArrayOutputStream))
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

;-------------------------------------------------------COMMON API------------------------------------------------------
(defn ->transit-string [data]
  (let [out (ByteArrayOutputStream.)
        writer (transit/writer out :json)]
    (transit/write writer data)
    (.toString out)))

;(jsonista/write-value-as-string x) is equivalent to, but faster than, (charred/write-json-str x {:escape-slash false})
;WARNING - TRANSIT (gpt) WILL MESS UP SOME UNICODE CHARACTERS, BETTER USE (gpj) FOR TEXT DATA

(defmulti pb (fn [x] (:encoding x)))
(defmethod pb :text          [x] {:status 200 :headers {"Content-Type" "text/plain"}               :body (jsonista/write-value-as-string (:data x))})
(defmethod pb :transit       [x] {:status 200 :headers {"Content-Type" "application/transit+json"} :body (->transit-string (:data x))})
(defmethod pb :json          [x] {:status 200 :headers {"Content-Type" "application/json"}         :body (jsonista/write-value-as-string (:data x))})
(defmethod pb :json-post     [x] {:status 201 :headers {"Content-Type" "application/json"}         :body (jsonista/write-value-as-string (:data x))})

(defn gpx [data] (pb {:encoding :text :data data}))
(defn gpt [data] (pb {:encoding :transit :data data}))
(defn gpj [data] (pb {:encoding :json :data data}))
(defn gpp [data] (pb {:encoding :json-post :data data}))

;-------------------------------------------------API CALLS-------------------------------------------------------------
;(defn get-wealth-summary    [req] (gpt @wealth/wealth-summary))
(defn get-positions-summary [req] (gpt @positions/positions-summary))
(defn get-bank-summary      [req] (gpt @bank/bank-summary))
(defn get-vault-summary     [req] (gpt @vault/vault-summary))
(defn get-cellar-summary    [req] (gpt @cellar/cellar-summary))
(defn get-price-history     [req] (gpt (marketdata/get-yahoo-price-history (:ticker (:params req)) "1wk" "2002-01-01")))

(defroutes app-routes
           ;(GET "/wealth-summary"                               [] get-wealth-summary)
           (GET "/positions-summary"                            [] get-positions-summary)
           (GET "/bank-summary"                                 [] get-bank-summary)
           (GET "/vault-summary"                                [] get-vault-summary)
           (GET "/cellar-summary"                               [] get-cellar-summary)

           (GET "/price-history"                                [] get-price-history)


           ;(POST "/quant-model-save-new-bond"       [] post-quant-model-save-new-bond!)
           (route/not-found print-request)
           )
;-------------------------------------------------SERVER----------------------------------------------------------------
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