(ns server.positions
  (:require
    [server.marketdata :as mdata]
    [server.static :as static]
    [server.tools :as t]
    )
  )

;------------------------------------------------POSITIONS-------------------------------------------------------
(defn get-clean-positions []
  (let [raw-positions (t/import-txt "positions.csv")
        yahoo-snapshot-data (group-by :ticker (mdata/get-yahoo-snapshot-data (map :ticker raw-positions)))
        fx-data-raw (mdata/get-yahoo-last-price static/list-fx)
        fx-data (->> fx-data-raw
                      (concat [{:ticker "GBpGBP=x" :fx 0.01}
                               {:ticker "GBpEUR=x" :fx (* (:fx (first (t/chainfilter {:ticker "GBPEUR=x"} fx-data-raw))) 0.01)}
                               {:ticker "EUREUR=x" :fx 1}])
                      (group-by :ticker))
        positions-with-market-data (for [p raw-positions] (merge p (first (yahoo-snapshot-data (:ticker p)))))
        positions-clean (->>  positions-with-market-data
                              (map #(assoc % :quantity (read-string (:quantity %))))
                              (map #(assoc % :cost-per-unit (read-string (:cost-per-unit %))))

                              (map #(assoc % :cost-value-local (* (:quantity %) (:cost-per-unit %))))
                              (map #(assoc % :nav-local (* (:quantity %) (:regularMarketPrice %))))
                              (map #(assoc % :pnl-local (- (:nav-local %) (:cost-value-local %))))
                              (map #(assoc % :pnl-local-perc (/ (:pnl-local %) (:cost-value-local %))))

                              (map #(assoc % :nav-eur (* (:nav-local %) (:fx (first (fx-data (str (:currency %) "EUR=x")))) )))
                              (map #(assoc % :pnl-eur (* (:pnl-local %) (:fx (first (fx-data (str (:currency %) "EUR=x")))) )))
                              (map #(assoc % :pnl-eur-perc (* (:pnl-local-perc %) (:fx (first (fx-data (str (:currency %) "EUR=x"))))))))
        nav-eur (reduce + (map :nav-eur positions-clean))
        positions-alloc (->>  positions-clean
                              (map #(assoc % :alloc-strat-1 (if (= (:strategy-1 %) "") 0 (* nav-eur (/ 100 (static/allocation-model (:strategy-1 %)))))))
                              (map #(assoc % :alloc-strat-2 (if (= (:strategy-2 %) "") 0 (* nav-eur (/ 100 (static/allocation-model (:strategy-2 %)))))))
                              (map #(assoc % :alloc-strat-3 (if (= (:strategy-3 %) "") 0 (* nav-eur (/ 100 (static/allocation-model (:strategy-3 %)))))))
                              (map #(assoc % :alloc-strat-total (+ (:alloc-strat-1 %) (:alloc-strat-2 %) (:alloc-strat-3 %))))
                              (map #(assoc % :alloc-strat-delta (- (:alloc-strat-total %) (:nav-eur %))))
                        )
        positions-final nil
        ]
    positions-clean
    )
  )

;------------------------------------------------POSITIONS ANALYTICS-------------------------------------------------------

;characteristics => ~holdinds, total EUR nav, total pnl EUR, PE, PB, Dyield
;per strat/sector/mktcap









