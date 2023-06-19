(ns server.bank
  (:require
    [server.marketdata :as mdata]
    [server.static :as static]
    [server.tools :as t]
    )
  )

(defn get-bank-summary []
  (let [raw-data (t/import-txt "bank.csv")
        raw-fx (mdata/get-yahoo-last-price static/list-fx)
        bank-clean (->>  raw-data
                         (map #(assoc % :value (read-string (:value %))))
                         (map #(assoc % :value-eur (if (= (:ccy %) "GBP")
                                                     (* (:value %) (:fx (first (t/chainfilter {:ticker "GBPEUR=x"} raw-fx))))
                                                     (:value %)
                                                     ))))
        bank-summary bank-clean]
    bank-summary
    )
  )
