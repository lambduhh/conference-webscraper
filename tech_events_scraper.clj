(ns tech-events-scraper
  (:require [clj-http.client :as client]
            [net.cgrand.enlive-html :as html]
            [clojure.data.csv :as csv]
            [clojure.java.io :as io]))

(defn fetch-page [url]
  (let [response (client/get url)]
    (html/html-resource (java.io.StringReader. (:body response)))))

(defn extract-events [page]
  (let [event-nodes (html/select page [:div.event-card])]
    (for [event event-nodes]
      (let [name (first (html/select event [:h3]))
            location (first (html/select event [:span.location]))
            link (first (html/select event [:a]))
            date (first (html/select event [:span.date]))]
        {:name (html/text name)
         :location (if location (html/text location) "N/A")
         :link (if link (html/attr= link :href) "N/A")
         :date (if date (html/text date) "N/A")}))))


(defn write-csv [filename data]
  (with-open [writer (io/writer filename)]
    (csv/write-csv writer (map #(vector (:name %) (:location %) (:link %) (:date %)) data))))

(defn -main []
  (let [url "https://www.bizzabo.com/blog/technology-events"
        page (fetch-page url)
        events (extract-events page)]
    (write-csv "technology_events.csv" events)
    (println "CSV file created successfully.")))

;; To run the script
(-main)
