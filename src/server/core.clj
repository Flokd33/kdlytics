(ns server.core
  (:require [server.market-data :as market-data]
            [server.positions :as positions]
            [server.static :as static]
            )
  )

(defn fx-data [] (market-data/get-yahoo-last-price static/list-fx))
(defn metals-data [] (market-data/get-yahoo-last-price static/list-metals))
(defn snapshot-data [] (market-data/get-yahoo-snapshot-data static/list-tickers))
(defn price-history-data [] (market-data/get-yahoo-price-history "BP.L" "1d" "2023-04-01"))
