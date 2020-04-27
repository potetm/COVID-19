(ns com.potetm.populations
  (:require [clojure.java.io :as io]
            [clojure.data.csv :as csv]
            [clojure.string :as str]))

(defn read-data [path]
  (with-open [r (io/reader (io/file path))]
    (let [[h & rows] (csv/read-csv r
                                   :separator \,
                                   :quote \")]
      (into []
            (map (fn [r]
                   (zipmap h r)))
            rows))))


(def us-state-pops
  (into {}
        (map (fn [{s "NAME"
                   pop "POPESTIMATE2019"}]
               [s (Long/parseLong pop)]))
        (read-data "populations/adult-population-us.csv")))

(def world-pops
  (into {}
        (map (fn [{c "Region, subregion, country or area *"
                   pop "2020"}]
               (let [pop (try
                           (Long/parseLong (str/replace pop #"\s" ""))
                           (catch NumberFormatException nfe))]
                 (when pop
                   [c (* pop 1000)]))))
        (read-data "populations/adult-population-world.csv")))

