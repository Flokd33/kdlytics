(ns server.static)


(def list-tickers ["RIO.L" "BP.L" "GLEN.L" "AAL.L" "LGEN.L" "MC.PA" "TTE.PA" "ORA.PA" "RUI.PA" "STMPA.PA"])
(def list-fx ["GBPEUR=x" "USDEUR=x"])
(def list-metals ["GC=F" "SI=F" "PL=F" "PA=F"])
(def list-field-price ["shortName" "currency" "regularMarketPrice" "marketCap"])
(def list-field-statistics ["52WeekChange" "beta" "priceToBook" "forwardPE" "enterpriseToEbitda" "dividendYield" "lastDividendValue" "trailingEps" "forwardEps" "bookValue" "profitMargins"])

(def list-field-snapshot ["shortName" "currency" "regularMarketPrice" "marketCap"
                           "52WeekChange" "beta" "priceToBook" "forwardPE" "enterpriseToEbitda" "dividendYield" "lastDividendValue" ;add trailing 12 months bla bla
                           "trailingEps" "forwardEps" "bookValue" "profitMargins"])

