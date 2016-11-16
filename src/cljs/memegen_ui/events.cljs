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

(defn filter-memes
  [available-memes filter-text]
  (filter (fn [meme]
            (let [search-matches
                  (reduce (fn [count meme-keyword]
                            (if (string/includes? (string/lower-case meme-keyword) filter-text)
                              (inc count)
                              count))
                          0
                          (:keywords meme))]
              (> search-matches 0)))
          available-memes))

(re-frame/reg-event-db
 :filter-text
 (fn [db [_ value]]
   (let [text (string/lower-case value)]
     (if-not (empty? text)
       (-> db
           (assoc :filter-text text)
           (assoc :filtered-meme-templates (filter-memes (:available-meme-templates db) text)))
       (-> db
           (assoc :filter-text text)
           (assoc :filtered-meme-templates (:available-meme-templates db)))))))

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
