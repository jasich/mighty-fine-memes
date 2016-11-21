(ns memegen-ui.events
  (:require [re-frame.core :as re-frame]
            [clojure.string :as string]
            [ajax.core :refer [GET]]
            [memegen-ui.db :as db]
            [memegen-ui.config :as config]))

(re-frame/reg-event-db
 :initialize-db
 (fn  [_ _]
   db/default-db))

(defn count-keyword-matches
  "Counts how many times each word in the filter text appears in
   the keywords for a meme"
  [keywords filter-text]
  (let [filter-words (map string/lower-case  (string/split filter-text #"\s"))
        lower-keywords (map string/lower-case keywords)]
    (reduce (fn [filter-count filter-word]
              (+ filter-count (reduce (fn [keyword-count keyword]
                           (if (string/includes? keyword filter-word)
                             (inc keyword-count)
                             keyword-count))
                         0
                         lower-keywords)))
            0
            filter-words)))

(defn rank-memes [memes filter-text]
  (map (fn [meme]
         (assoc meme :rank (count-keyword-matches (:keywords meme) filter-text)))
       memes))

(defn filter-memes [available-memes filter-text]
  (let [text (string/lower-case filter-text)
        ranked-memes (rank-memes available-memes text)
        total-matches (reduce (fn [total item] (+ total (:rank item))) 0 ranked-memes)]
    (if (or (> total-matches 0) (not (empty? filter-text)))
      (filter #(> (:rank %) 0) ranked-memes)
      ranked-memes)))

(defn sort-memes [memes filtered?]
  (if filtered?
    (reverse (sort-by :rank memes))
    (sort-by :name memes)))

(re-frame/reg-event-db
 :filter-text
 (fn [db [_ filter-text]]
   (let [available-memes (:available-meme-templates db)
         filtered-memes (filter-memes available-memes filter-text)
         sorted-filtered-memes (sort-memes filtered-memes (not (empty? filter-text)))]
     (-> db
         (assoc :filter-text filter-text)
         (assoc :filtered-meme-templates sorted-filtered-memes)))))

(defn template-handler [response]
  (re-frame/dispatch [:process-templates-reponse response]))

(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "something bad happened: " status " " status-text)))

(re-frame/reg-event-db
 :get-init-data
 (fn [db _]
   (GET config/api-url
        {:handler template-handler
         :error-handler error-handler})
   (assoc db :loading? true)))

(defn map-template-response [response]
  (map (fn [search-item]
         (let [template (get search-item "template")]
           (reduce (fn [mapped-template [key value]]
                     (assoc mapped-template (keyword key) value))
                   {}
                   template)))
       response))

(re-frame/reg-event-db
 :process-templates-reponse
 (fn [db [_ response]]
   (let [templates (map-template-response (js->clj response))]
     (-> db
         (assoc :loading? false)
         (assoc :initialized? true)
         (assoc :filtered-meme-templates templates)
         (assoc :available-meme-templates templates)))))
