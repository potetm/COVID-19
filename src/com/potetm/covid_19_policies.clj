(ns com.potetm.covid-19-policies)

(def policy-changes
  (into {}
        (map (fn [[k vs]]
               [k (mapv (fn [v]
                          (assoc v :y [100]))
                        vs)]))
        {"Norway" [{:x [#inst"2020-03-12"]
                    :policy-type "Full Lockdown"
                    :source "https://web.archive.org/web/20200312193349/https://www.lifeinnorway.net/norway-is-closed-coronavirus/"}
                   {:x [#inst"2020-04-27"]
                    :policy-type "Reopening"
                    :source "https://www.lifeinnorway.net/coronavirus-in-norway/"
                    :source-note "As of 2020-04-27. Check wayback machine for permalink."}]
         "Finland" [{:x [#inst"2020-03-28"]
                     :policy-type "Regional Lockdown"
                     :source "https://www.channelnewsasia.com/news/world/covid19-finland-lifts-lockdown-helsinki-uusimaa-roadblock-travel-12643976"}
                    {:x [#inst"2020-04-15"]
                     :policy-type "Reopening"
                     :source "https://www.channelnewsasia.com/news/world/covid19-finland-lifts-lockdown-helsinki-uusimaa-roadblock-travel-12643976"}]
         "Arkansas" [{:x [#inst"2020-03-18"]
                      :policy-type "School Closing"
                      :source "https://www.livescience.com/coronavirus-arkansas.html"}
                     {:x [#inst"2020-03-19"]
                      :policy-type "Restaurant Closing"
                      :source "https://www.livescience.com/coronavirus-arkansas.html"}
                     {:x [#inst"2020-04-04"]
                      :policy-type "Hotel Restrictions"
                      :source "https://www.livescience.com/coronavirus-arkansas.html"}]
         "Tennessee" [{:x [#inst"2020-03-16"]
                       :policy-type "School Closing"
                       :source "https://www.livescience.com/tennessee-coronavirus-updates.html"}
                      {:x [#inst"2020-03-22"]
                       :policy-type "Business Closing"
                       :source "https://www.livescience.com/tennessee-coronavirus-updates.html"}
                      {:x [#inst"2020-03-30"]
                       :policy-type "Full Lockdown"
                       :source "https://www.livescience.com/tennessee-coronavirus-updates.html"}
                      {:x [#inst"2020-04-27"]
                       :policy-type "Partial Reopening"
                       :source "https://www.livescience.com/tennessee-coronavirus-updates.html"}]
         "Sweden" [{:x [#inst"2020-03-11"]
                    :policy-type "Assembly Ban"
                    :source "https://en.wikipedia.org/wiki/2020_coronavirus_pandemic_in_Sweden#Government_response"}
                   {:x [#inst"2020-03-20"]
                    :policy-type "Assembly Ban"
                    :source "https://en.wikipedia.org/wiki/2020_coronavirus_pandemic_in_Sweden#Government_response"}
                   {:x [#inst"2020-03-24"]
                    :policy-type "Restaurant Restriction"
                    :source "https://en.wikipedia.org/wiki/2020_coronavirus_pandemic_in_Sweden#Government_response"}]
         "New York" [{:x [#inst"2020-03-20"]
                      :policy-type "Full Lockdown"
                      :source "https://www.livescience.com/coronavirus-new-york.html"}]
         "California" [{:x [#inst"2020-03-16"]
                        :policy-type "Regional Lockdown"
                        :source "https://www.livescience.com/california-coronavirus-updates.html"}
                       {:x [#inst"2020-03-19"]
                        :policy-type "Full Lockdown"
                        :source "https://www.livescience.com/california-coronavirus-updates.html"}]}))

