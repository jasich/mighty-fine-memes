(ns memegen-ui.events
  (:require [re-frame.core :as re-frame]
            [ajax.core :refer [GET]]
            [memegen-ui.db :as db]))

(re-frame/reg-event-db
 :initialize-db
 (fn  [_ _]
   db/default-db))

(defn handler [response]
  (re-frame/dispatch [:process-templates-reponse response]))

(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "something bad happened: " status " " status-text)))

(re-frame/reg-event-db
 :get-init-data
 (fn [db _]
   (GET "https://memegen.link/api/templates/"
        {:handler handler
         :error-handler error-handler})
   (assoc db :loading? true)))

(def response-keys [:text :link])
(defn map-response
  [response]
  (map (fn [un-mapped]
         (reduce (fn [row-map [key value]]
                   (assoc row-map key value))
                 {}
                 (map vector response-keys un-mapped)))
       response))

(re-frame/reg-event-db
 :process-templates-reponse
 (fn
   [db [_ response]]
   (let [templates (map-response (js->clj response))]
     (-> db
         (assoc :loading? false)
         (assoc :initialized? true)
         (assoc :available-meme-templates (js->clj templates))))))
