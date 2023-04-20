(ns flo.positions)


; import raw data from static
; clean and add market data


(defn import-txt [path] "Open and transform a .txt into a list of maps - separator is a comma" (let [txt-raw (slurp "C:\Users\fcadet\Documents\Florian\test.txt" ) txt-clean (for [line (clojure.string/split-lines txt-raw)] (for [word (clojure.string/split line #",")] (clojure.string/trim word))) txt-final (map #(zipmap (map keyword (first txt-clean)) %) (rest txt-clean))] txt-final ))