(ns server.vault
  (:require
    [server.tools :as t]
    [server.marketdata :as marketdata]
    )
  )

(def vault-summary (atom nil))

;TODO do we create a PNL/transaction summary AND a inventory ? I think Yes
;TODO catalogue with characteristics

(defn get-vault-summary []
  (let [raw-data (t/import-txt "vault.csv")
        fx-data (group-by :ticker @marketdata/fx-data)
        market-data (group-by :ticker @marketdata/commodities-data)
        clean-data (->>  raw-data
                         (map #(assoc % :quantity (read-string (:quantity %))))
                         (map #(assoc % :price (read-string (:price %))))
                         (map #(assoc % :spot_usd (read-string (:spot_usd %))))

                         ;add item_characteristics (from catalogue flat file ?) fine weight oz, fine weight gr, total weight etc...

                         (map #(assoc % :cost (* (:price %) (:quantity %))))
                         ;(map #(assoc % :fine_weight_oz (* (:price %) (:quantity %)))) ;from catalogue
                         (map #(assoc % :cost_eur (if (= (:ccy %) "EUR")
                                                    (:cost %)
                                                    (* (:cost %) (:value (first ((str (:ccy %) "EUR=x") fx-data)))))))
                         ;premium

                         (map #(assoc % :market_value_usd (* (:fine_weight_oz %) (:value (first (market-data (:ore_code %)))))))
                         (map #(assoc % :market_value_eur (/ (:market_value_usd %) (:value (first (fx-data "USDEUR=x"))))))

                         (map #(assoc % :pnl_eur (- (:market_value_eur %) (:cost_eur %))))
                         (map #(assoc % :pnl_eur_perc (- (:market_value_eur %) (:cost_eur %))))

                         )]
    clean-data))

(defn refresh-vault-data! []
  (reset! vault-summary (get-vault-summary)))