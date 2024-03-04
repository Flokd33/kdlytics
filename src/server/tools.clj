(ns server.tools
  (:require [jsonista.core :as jsonista]
            [clojure.string]
            )
  )

(defn json->kmap [^String s] (jsonista/read-value s (jsonista/object-mapper {:decode-key-fn true})))

(defn chainfilter
  "Chain filter (boolean AND). Defaults to equality if predicate is not a function.
  warning: only one filter per key (no duplicates)
  example: (chainfilter {:portfolio #(= % \"OGEMCORD\") :weight pos?} @positions)
  equivalent to (chainfilter {:portfolio \"OGEMCORD\" :weight pos?} @positions)"
  [m coll]
  (reduce-kv
    (fn [erg k pred]
      (filter #(if (fn? pred) (pred (get % k)) (= pred (get % k))) erg)) coll m))

(defn import-txt [path]
  "Open and transform a .txt into a list of maps - separator is a comma"
  (let [txt-raw (slurp path)
        txt-clean (for [line (clojure.string/split-lines txt-raw)]
                    (for [word (clojure.string/split line #",")]
                      (clojure.string/trim word)))
        txt-final (map #(zipmap (map keyword (first txt-clean)) %) (rest txt-clean))]
    txt-final ))