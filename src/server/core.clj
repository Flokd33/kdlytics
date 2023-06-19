(ns server.core
  (:require [server.marketdata :as marketdata]
            [server.positions :as positions]
            [server.static :as static]
            )
  )

;TODO insert (-main)  that trigger all need fct ? fx first and then rest ?

(defn fx-data [] (marketdata/get-yahoo-last-price static/list-fx))
(defn metals-data [] (marketdata/get-yahoo-last-price static/list-metals))
;(defn snapshot-data [] (marketdata/get-yahoo-snapshot-data static/list-tickers))
(defn price-history-data [] (marketdata/get-yahoo-price-history "BP.L" "1d" "2023-04-01"))
