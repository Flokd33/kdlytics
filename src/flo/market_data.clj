(ns flo.market-data
  (:require [cheshire.core :as cheshire]
            ))
;(:import java.time.LocalDate)
;(LocalDate/parse "2019-01-01")

;----------------------------------------------EXTRACT FROM YAHOO FINANCE-----------------------------------
;certainly the best and easier for prices but not for fundamental/valuation metrics
;https://github.com/dakrone/clj-http
;(slurp) will return the html content of a http.. basically the body of the http/get answer
;can be used slurp for text file, mayve csv ?

(def list-tickers ["RIO.L" "BP.L" "GLEN.L" "AAL.L" "LGEN.L" "MC.PA" "TTE.PA" "ORA.PA" "RUI.PA" "STMPA.PA"])

(def query-head-historical-prices "https://query1.finance.yahoo.com/v7/finance/chart/")
(def query-tail-historical-prices "?")

(def query-head-snapshot "https://query2.finance.yahoo.com/v10/finance/quoteSummary/")
(def query-tail-price "?modules=price")
(def query-tail-statistics "?modules=defaultKeyStatistics")
;(def query-tail-fundamentals "?modules=incomeStatementHistoryQuarterly")

(def list-field-price ["shortName" "currency" "regularMarketPrice" "marketCap"])
(def list-field-statistics ["52WeekChange" "beta" "priceToBook" "forwardPE" "enterpriseToEbitda" "lastDividendValue" "trailingEps" "forwardEps" "bookValue" "profitMargins"])
;(def list-field-fundamentals ["..."])




(defn get-yahoo-snapshot-data [list-tickers]
  "Extract snapshot data from yahoo finance for a list of tickers...static data, key stats and last price"
  (let [price-data (flatten
                      (for [ticker list-tickers]
                        (for [field list-field-price]
                          (let [results (get (first (vals (first (get-in (cheshire.core/parse-string (slurp (str query-head-snapshot ticker query-tail-price))) ["quoteSummary" "result"])))) field)]
                            (into {}
                              {:ticker ticker
                               (keyword field) (if (or (= field "shortName") (= field "currency"))
                                                 results
                                                 (get results "raw"))})))))
        statistics-data (flatten
                          (for [ticker list-tickers]
                            (for [field list-field-statistics]
                              (let [results (get (first (vals (first (get-in (cheshire.core/parse-string (slurp (str query-head-snapshot ticker query-tail-statistics))) ["quoteSummary" "result"])))) field)]
                                (into {}
                                      {:ticker ticker
                                       (keyword field) (get results "raw")})))))
        fundamental-data nil
        snapshot-data (let [data-grouped (group-by :ticker (concat price-data statistics-data))]
                        (for [ticker list-tickers]
                          (assoc (into {} (map #(second %) (data-grouped ticker))) :ticker ticker)))
        ]
    snapshot-data
    )
  )


(defn get-yahoo-price-history [ticker period start-date]    ;(get-yahoo-price-history "BP.L" "1d" "2023-04-01")
  "Get historical price data from Yahoo finance for a given ticker - period is one of 1m, 5m, 15m, 30m, 90m, 1h, 1d, 5d, 1wk, 1mo, 3mo - start-date format is YYYY-MM-DD "

 (let [list-field ["date" "open" "high" "low" "close" "adj-close" "volume"]
       start-date-unix (int (/ (.getTime (read-string (str "#inst \"" start-date "\"" ))) 1000)) ;(.getTime #inst"1988-07-04T00:00:00.000-00:00")
       end-date-unix (int (/ (.getTime (java.util.Date.)) 1000))
       query-head "https://query1.finance.yahoo.com/v7/finance/download/"
       query-tail (str "?period1=" start-date-unix "&period2=" end-date-unix "&interval=" period "&events=history")
       raw-results (slurp (str query-head ticker query-tail))
       clean-results (for [line (clojure.string/split-lines raw-results)] (for [word (clojure.string/split line #",")] (clojure.string/trim word)))
       historical-price-data (map #(zipmap (map keyword list-field) %) (rest clean-results)) ;instead of list field we can use keyword of first results with fct below  ;;;;;;;;;;; (clojure.string/join "" (filter #(not (clojure.string/blank? %)) (clojure.string/split " AAAA TTT " #" "))) ;;;;;;;;;;
       ]
   historical-price-data))

(defn get-yahoo-fx []
  "Extract FX from yahoo finance"
  (let [fx-data nil
        ]
    fx-data
    )
  )












