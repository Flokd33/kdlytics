(ns server.core
  (:require
    [server.marketdata :as marketdata]
    ;[server.static :as static]
    [clojure.tools.logging :as log]
    [server.api :as api]
    )
  )

;TODO insert (-main) that trigger all need fct ? fx first and then rest?

;(defn fx-data [] (marketdata/get-yahoo-last-price static/list-fx))
;(defn metals-data [] (marketdata/get-yahoo-last-price static/list-metals))
;(defn snapshot-data [] (marketdata/get-yahoo-snapshot-data static/list-tickers))
;(defn price-history-data [] (marketdata/get-yahoo-price-history "BP.L" "1d" "2023-04-01"))


(defn -main [& args]
  ;(log/info "Starting API server on port 3501...")
  ;(api/start-web-server! 3501)
  (api/daily-run!)
  ;(log/info "Setting up schedules")
  ;(let [schedules [{:fn tadb/daily-run!                   :start (ms-to-time 2 0 0)}]] (doseq [line schedules] (.scheduleAtFixedRate daily-scheduler-pool (try-catch-throw #((:fn line))) (:start line) (* 24 3600 1000) TimeUnit/MILLISECONDS)))
  (log/info "Ready!"))