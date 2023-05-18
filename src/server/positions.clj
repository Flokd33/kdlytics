(ns server.positions
  (:require
    [server.marketdata :as mdata]
    [server.static :as static]
    [server.tools :as t]
    )
  )

;TODO handle cash, remove from ticker list for market data and add back after

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
                              (map #(assoc % :nb-strategies  1))

                              (map #(assoc % :cost-value-local (* (:quantity %) (:cost-per-unit %))))
                              (map #(assoc % :nav-local (* (:quantity %) (:regularMarketPrice %))))
                              (map #(assoc % :pnl-local (- (:nav-local %) (:cost-value-local %))))
                              (map #(assoc % :pnl-local-perc (/ (:pnl-local %) (:cost-value-local %))))

                              (map #(assoc % :nav-eur (* (:nav-local %) (:fx (first (fx-data (str (:currency %) "EUR=x")))) )))
                              (map #(assoc % :pnl-eur (* (:pnl-local %) (:fx (first (fx-data (str (:currency %) "EUR=x")))) )))
                              (map #(assoc % :pnl-eur-perc (* (:pnl-local-perc %) (:fx (first (fx-data (str (:currency %) "EUR=x"))))))))
        nav-eur (reduce + (map :nav-eur positions-clean))
        allocation-count (into {}
                           (for [strat (keys static/allocation-model)]
                           [strat (count (filter #(= % strat) (concat (map :strategy-3 positions-clean) (map :strategy-2 positions-clean) (map :strategy-1 positions-clean))))]))
        positions-alloc (->>  positions-clean
                              (map #(assoc % :nav-eur-perc (/ (:nav-eur %) nav-eur )))
                              (map #(assoc % :alloc-strat-1 (if (= (:strategy-1 %) "") 0 (/ (* nav-eur (/ (static/allocation-model (:strategy-1 %)) 100)) (allocation-count (:strategy-1 %))))))
                              (map #(assoc % :alloc-strat-2 (if (= (:strategy-2 %) "") 0 (/ (* nav-eur (/ (static/allocation-model (:strategy-2 %)) 100 )) (allocation-count (:strategy-2 %))))))
                              (map #(assoc % :alloc-strat-3 (if (= (:strategy-3 %) "") 0 (/ (* nav-eur (/ (static/allocation-model (:strategy-3 %)) 100 )) (allocation-count (:strategy-3 %))))))
                              (map #(assoc % :alloc-strat-total (+ (:alloc-strat-1 %) (:alloc-strat-2 %) (:alloc-strat-3 %))))
                              (map #(assoc % :alloc-strat-delta (- (:alloc-strat-total %) (:nav-eur %))))
                        )]
    positions-alloc
    )
  )

;(get top-10 )
;...


(defn get-clean-positions-with-analytics []
  (let [clean-positions (get-clean-positions)
        top10 (map #(select-keys % [:nav-eur-perc :nav-eur :ticker]) (take 10 (reverse (sort-by :nav-eur-perc clean-positions))))
        characteristics [{:metric "nav-eur" :value (reduce + (map :nav-eur clean-positions))}
                         {:metric "count-x-cash" :value (count (t/chainfilter {:strategy-1 #(not (= % "CASH"))} clean-positions))}
                         {:metric "count-etf" :value (count (t/chainfilter {:asset-class #(= % "ETF")} clean-positions))}
                         {:metric "top10-weight" :value (reduce + (map :nav-eur-perc top10))}
                         {:metric "cash" :value (reduce + (map :nav-eur-perc (t/chainfilter {:strategy-1 #(= % "CASH")} clean-positions)))}
                         {:metric "div" :value (reduce + (map #(* (:nav-eur-perc %) (:dividendYield %)) clean-positions))}
                         {:metric "fpe" :value (reduce + (map #(* (:nav-eur-perc %) (:forwardPE %)) clean-positions))}
                         {:metric "pb" :value (reduce + (map #(* (:nav-eur-perc %) (:priceToBook %)) clean-positions))}
                         {:metric "beta" :value (reduce + (map #(* (:nav-eur-perc %) (:beta %)) clean-positions))}
                         ]
        exposure-strategy-1 (for [strat (group-by :strategy-1 clean-positions)] {:strategy-1 (key strat) :nav-eur-perc (reduce + (map :nav-eur-perc (val strat)))})
        ;TODO allocation total strat 123, kinda weight / count of strat, for each strat
        ]
    [clean-positions top10 characteristics exposure-strategy-1]
    ))

;-----------------------------------------------all needed---------------

;get clean postions
;get-positons analytics using clean postions
;refresh atom with the 2







