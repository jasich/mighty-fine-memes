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

(re-frame/reg-event-db
 :filter-text
 (fn [db [_ value]]
   (let [text (string/lower-case value)]
     (if-not (empty? text)
       (-> db
           (assoc :filter-text text)
           (assoc :filtered-meme-templates (filter #(string/includes? (string/lower-case (:name %)) text) (:available-meme-templates db))))
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

(def meme-template-keys
  "The keywords used to define a meme-template "
  [:name :link])

(defn map-template-response
  "Maps the incoming response hash-map that uses meme names for keys and links for
  values into a hash-map that uses the keywords in `meme-template-keys` for keys."
  [response]
  (map (fn [key-value-pair]
         (reduce (fn [mapped-template [key value]] (assoc mapped-template key value))
                 {}
                 (map vector meme-template-keys key-value-pair)))
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
