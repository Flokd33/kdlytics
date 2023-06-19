(ns server.cellar
  (:require
    [server.tools :as t])
  )


(defn get-cellar-summary []
  (let [raw-data (t/import-txt "cellar.csv")
        clean-data (->>  raw-data
                         (map #(assoc % :quantity (read-string (:quantity %))))
                         (map #(assoc % :cost-per-unit (read-string (:cost-per-unit %))))
                         (map #(assoc % :market_price (read-string (:market_price %))))
                         ;add ???
                         )
        ])
  )