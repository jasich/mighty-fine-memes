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

(defn count-keyword-matches [keywords filter-text]
  (reduce (fn [count meme-keyword]
            (if (string/includes? (string/lower-case meme-keyword) filter-text)
                              (inc count)
                              count))
          0
          keywords))

(defn filter-memes [available-memes filter-text]
  (let [text (string/lower-case filter-text)]
    (if-not (empty? text)
      (filter (fn [meme]
                (let [keyword-matches (count-keyword-matches (:keywords meme) text)]
                  (> keyword-matches 0)))
              available-memes)
      available-memes)))

(re-frame/reg-event-db
 :filter-text
 (fn [db [_ value]]
   (let [available-memes (:available-meme-templates db)
         filtered-memes (filter-memes available-memes value)]
     (-> db
         (assoc :filter-text value)
         (assoc :filtered-meme-templates filtered-memes)))))

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
