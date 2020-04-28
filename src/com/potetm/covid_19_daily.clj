(ns com.potetm.covid-19-daily
  (:require [clojure.data.csv :as csv]
            [com.hypirion.clj-xchart :as chart]
            [clojure.java.io :as io]
            [com.potetm.populations :as pops]
            [clojure.string :as str])
  (:import (java.time.format DateTimeFormatter)
           (java.time LocalDate ZoneId Instant)
           (java.util Date)))

(def ^DateTimeFormatter date-format
  (DateTimeFormatter/ofPattern "M-d-yyyy"))

(def ^DateTimeFormatter date-time-format
  (-> (DateTimeFormatter/ofPattern "yyyy-M-d H:m:s")
      (.withZone (ZoneId/of "UTC"))))

(def long* (fnil #(Long/parseLong %)
                 nil))

(def coerce
  {"Last_Update" (fn [v]
                   (Instant/from (.parse date-time-format
                                         v)))
   "Confirmed" long*
   "Deaths" long*
   "Recovered" long*
   "Active" long*})

(defn trim-to-nil [s]
  (let [s (str/trim s)]
    (when-not (= s "")
      s)))

(defn read-data [p]
  (with-open [r (io/reader (io/file p))]
    (let [day (LocalDate/parse (second (re-find #"/([0-9-]+).csv$" p))
                               date-format)
          [h & rows] (csv/read-csv r
                                   :separator \,
                                   :quote \")]
      (into []
            (comp (map (fn [r]
                         (zipmap h
                                 (map trim-to-nil r))))
                  (map (fn [d]
                         (into {:date day}
                               (map (fn [[k v]]
                                      (if-some [c (coerce k)]
                                        [k (c v)]
                                        [k v])))
                               d))))
            rows))))

(comment
  (take 10 (read-data "csse_covid_19_data/csse_covid_19_daily_reports/04-23-2020.csv"))


  (re-find #"/([0-9-]+).csv$"
           "csse_covid_19_data/csse_covid_19_daily_reports/04-23-2020.csv")
  )
