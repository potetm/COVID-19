(ns com.potetm.covid-19-ts
  (:require [clojure.data.csv :as csv]
            [com.hypirion.clj-xchart :as chart]
            [clojure.java.io :as io]
            [com.potetm.populations :as pops]
            [com.potetm.covid-19-policies :as policy])
  (:import (java.time.format DateTimeFormatter)
           (java.time LocalDate ZoneId)
           (java.util Date)))

(def ^DateTimeFormatter date-format
  (DateTimeFormatter/ofPattern "M/d/yy"))

;; rhc = row-header-count
(defn read-ts-data [{rhc :row-header-count
                     p :path}]
  (with-open [r (io/reader (io/file p))]
    (let [[h & rows] (csv/read-csv r
                                   :separator \,
                                   :quote \")
          loc-data (take rhc h)
          days (map (fn [d]
                      (Date/from (-> (LocalDate/parse d date-format)
                                     (.atStartOfDay (ZoneId/of "UTC"))
                                     (.toInstant))))
                    (drop rhc h))]
      (into []
            (map (fn [r]
                   (assoc (zipmap loc-data
                                  (take rhc r))
                     :data (map (fn [day dat]
                                  {:date day
                                   :count (Long/parseLong dat)})
                                days
                                (drop rhc r)))))
            rows))))

(def global-ts-deaths
  {:row-header-count 4
   :path "csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_deaths_global.csv"})

(def us-ts-deaths
  {:row-header-count 12
   :path "csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_deaths_US.csv"})

(def global-ts-confirmed
  {:row-header-count 4
   :path "csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_confirmed_global.csv"})

(def us-ts-confirmed
  {:row-header-count 11
   :path "csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_confirmed_US.csv"})

(def us-death (read-ts-data us-ts-deaths))
(def global-death (read-ts-data global-ts-deaths))
(def us-confirmed (read-ts-data us-ts-confirmed))
(def global-confirmed (read-ts-data global-ts-confirmed))

(defn by-state [data]
  (into {}
        (map (fn [[s dat]]
               [s {:data (apply map
                                (fn [& days]
                                  (let [d (:date (first days))]
                                    {:date d
                                     :count (reduce +
                                                    0
                                                    (map :count
                                                         days))}))
                                (map :data dat))}]))
        (group-by #(get % "Province_State")
                  data)))

(defn by-country [data]
  (into {}
        (map (juxt #(get % "Country/Region")
                   identity))
        data))

(def death-by-state
  (by-state us-death))

(def death-by-country
  (by-country global-death))

(def confirmed-by-state
  (by-state us-confirmed))

(def confirmed-by-country
  (by-country global-confirmed))

(defn ts->chart-data [d]
  {:x (mapv :date d)
   :y (mapv :count d)})

(defn death-place-data [place]
  (:data ((some-fn death-by-state
                   death-by-country)
          place)))

(defn confirmed-place-data [place]
  (:data ((some-fn confirmed-by-state
                   confirmed-by-country)
          place)))

(defn pop-data [place]
  ((some-fn pops/us-state-pops
            pops/world-pops)
   place))

(defn per-capita [place data]
  (let [pop (pop-data place)]
    (map (fn [{c :count :as dp}]
           (assoc dp
             :count (* (/ c
                          pop)
                       1000000)))
         data)))

(defn daily-change [data]
  (keep (fn [[{c1 :count} {d :date
                           c2 :count}]]
          (when (and c1 c2)
            {:date d
             :count (Math/abs ^long (- c2 c1))}))
        (partition-all 2 1 data)))

(defn policy-changes [place]
  (into {}
        (map (fn [{pt :policy-type :as v}]
               [(str place " " pt) v]))
        (policy/policy-changes place)))

(def policy-changes-xf
  (mapcat (fn [[place _ :as dat]]
            [dat
             (policy-changes place)])))

(defn non-zero [{c :count :as dp}]
  (assoc dp
    :count (if (zero? c)
             0.001
             c)))

(defn confirmed-ts-compare [places]
  (chart/view
    (chart/xy-chart
      (into {}
            (comp (map (fn [place]
                         [place (merge (ts->chart-data
                                         (confirmed-place-data place))
                                       {:style {:marker-type :none}})]))
                  policy-changes-xf)
            places)
      {:title "COVID-19 Confirmed Cases"})))

(defn confirmed-ts-log-compare [places]
  (chart/view
    (chart/xy-chart
      (into {}
            (comp (map (fn [place]
                         [place (merge (ts->chart-data
                                         (map non-zero
                                              (confirmed-place-data place)))
                                       {:style {:marker-type :none}})]))
                  policy-changes-xf)
            places)
      {:title "COVID-19 Confirmed Cases (log)"
       :y-axis {:logarithmic? true}})))

(defn death-ts-compare [places]
  (chart/view
    (chart/xy-chart
      (into {}
            (map (fn [place]
                   [place (merge (ts->chart-data
                                   (death-place-data place))
                                 {:style {:marker-type :none}})]))
            places)
      {:title "COVID-19 Deaths"})))

(defn confirmed-ts-per-capita-compare [places]
  (chart/view
    (chart/xy-chart
      (into {}
            (comp (map (fn [place]
                         [place (merge (ts->chart-data
                                         (per-capita place
                                                     (confirmed-place-data place)))
                                       {:style {:marker-type :none}})]))
                  policy-changes-xf)
            places)
      {:title "COVID-19 Confirmed Cases per Million Residents"})))

(defn confirmed-ts-per-capita-log-compare [places]
  (chart/view
    (chart/xy-chart
      (into {}
            (comp (map (fn [place]
                         [place (merge (ts->chart-data
                                         (map non-zero
                                              (per-capita place
                                                          (confirmed-place-data place))))
                                       {:style {:marker-type :none}})]))
                  policy-changes-xf)
            places)
      {:title "COVID-19 Confirmed Cases per Million Residents (log)"
       :y-axis {:logarithmic? true}})))

(defn death-ts-per-capita-compare [places]
  (chart/view
    (chart/xy-chart
      (into {}
            (comp (map (fn [place]
                         [place (merge (ts->chart-data
                                         (per-capita place
                                                     (death-place-data place)))
                                       {:style {:marker-type :none}})]))
                  policy-changes-xf)
            places)
      {:title "COVID-19 Deaths per Million Residents"})))

(defn deaths-per-day [places]
  (chart/view
    (chart/category-chart
      (into {}
            (comp (map (fn [place]
                         [place (merge (ts->chart-data
                                         (daily-change (death-place-data place)))
                                       {:style {:marker-type :none}})]))
                  policy-changes-xf)
            places)
      {:title "COVID-19 Deaths per Day"
       :date-pattern "dd"})))

(defn deaths-per-day-per-capita [places]
  (chart/view
    (chart/category-chart
      (into {}
            (comp (map (fn [place]
                         [place (merge (ts->chart-data
                                         (per-capita place
                                                     (daily-change (death-place-data place))))
                                       {:style {:marker-type :none}})]))
                  policy-changes-xf)
            places)
      {:title "COVID-19 Deaths per Day per Million Residents"
       :date-pattern "dd"})))

(defn per-day-change [places]
  (chart/view
    (chart/category-chart
      (into {}
            (comp (map (fn [place]
                         [place (merge (ts->chart-data
                                         (drop 40 (daily-change (confirmed-place-data place))))
                                       {:style {:marker-type :none}})]))
                  policy-changes-xf)
            places)
      {:title "COVID-19 New Cases per Day"
       :date-pattern "dd"})))

(defn per-day-change-per-capita [places]
  (chart/view
    (chart/category-chart
      (into {}
            (comp (map (fn [place]
                         [place (merge (ts->chart-data
                                         (drop 40 (per-capita place
                                                              (daily-change (confirmed-place-data place)))))
                                       {:style {:marker-type :none}})]))
                  policy-changes-xf)
            places)
      {:title "COVID-19 New Cases per Day per Million Residents"
       :date-pattern "dd"})))

(comment
  (def countries [#_ "New York"
                  #_"California"
                  "Sweden"
                  #_"Finland"
                  #_"Norway"
                  "Arkansas"
                  #_"Tennessee"])
  (death-ts-compare countries)
  (death-ts-per-capita-compare countries)
  (deaths-per-day countries)
  (deaths-per-day-per-capita countries)

  (confirmed-ts-compare countries)
  (confirmed-ts-log-compare countries)
  (confirmed-ts-per-capita-compare countries)

  (confirmed-ts-per-capita-log-compare countries)

  (per-day-change countries)
  (per-day-change-per-capita countries)

  )
