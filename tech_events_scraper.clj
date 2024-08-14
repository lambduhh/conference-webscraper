(ns tech-events-scraper
  (:require [clj-http.client :as client]
            [net.cgrand.enlive-html :as html]
            [clojure.data.csv :as csv]
            [clojure.java.io :as io]))

(defn fetch-page [url]
  (let [response (client/get url)]
    (html/html-resource (java.io.StringReader. (:body response)))))

(defn extract-events [page]
  (let [event-nodes (html/select page [:div.EventCard])]
    (for [event event-nodes]
      (let [name (first (html/select event [:h3.EventCard-title]))
            location (first (html/select event [:div.EventCard-location]))
            link-node (first (html/select event [:a]))
            date (first (html/select event [:time]))
            link (if link-node (:href (:attrs link-node)))]
        {:name (html/text name)
         :location (if location (html/text location) "N/A")
         :link (if link (str "https://dev.events" link) "N/A")
         :date (if date (html/text date) "N/A")}))))

(defn write-csv [filename data]
  (with-open [writer (io/writer filename)]
    (csv/write-csv writer
                   ;; Ensure header and each row are fully realized vectors
                   (cons ["Name" "Location" "Link to website" "Estimated date"]
                         (mapv #(vector (:name %) (:location %) (:link %) (:date %)) data)))))

(defn -main []
  (let [url "https://dev.events/"
        page (fetch-page url)
        events (extract-events page)]
    (write-csv "dev_events.csv" events)
    (println "CSV file created successfully.")))

;; To run the script
(-main)
