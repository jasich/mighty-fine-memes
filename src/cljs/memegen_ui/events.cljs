(ns memegen-ui.events
  (:require [re-frame.core :as re-frame]
            [ajax.core :refer [GET]]
            [memegen-ui.config :as config]
            [memegen-ui.db :as db]
            [memegen-ui.search :refer [search]]))

(defonce timeouts
  (atom {}))

;; custom fx
(re-frame/reg-fx
 :dispatch-debounce
 (fn [[id event-vec n]]
   (js/clearTimeout (@timeouts id))
   (swap! timeouts assoc id
          (js/setTimeout (fn []
                           (re-frame/dispatch event-vec)
                           (swap! timeouts dissoc id))
                         n))))

;; fx events
(re-frame/reg-event-fx
 :update-filtered-memes
 (fn [_ [_ filter-text]]
   {:dispatch-debounce [::filter [:filter-memes filter-text] 250]}))


;; db events
(re-frame/reg-event-db
 :initialize-db
 (fn  [_ _]
   db/default-db))

(re-frame/reg-event-db
 :filter-memes
  (fn [db [_ filter-text]]
    (let [available-memes (:available-meme-templates db)
          filtered-memes (search filter-text available-memes)]
      (-> db
          (assoc :filter-text filter-text)
          (assoc :filtered-meme-templates filtered-memes)))))

(re-frame/reg-event-db
 :filter-text-updated
 (fn [db [_ filter-text]]
   (re-frame/dispatch [:update-filtered-memes filter-text])
   (-> db (assoc :filter-text filter-text))))

(defn api-search-handler [response]
  (re-frame/dispatch [:process-templates-reponse response]))

(defn api-error-handler [{:keys [status status-text]}]
  (.log js/console (str "something bad happened: " status " " status-text))
  (re-frame/dispatch [:api-error]))

(re-frame/reg-event-db
 :get-init-data
 (fn [db _]
   (GET config/api-search-url
        {:handler api-search-handler
         :error-handler api-error-handler})
   (assoc db :loading? true)))

(defn convert-keys-to-keywords [raw-template]
  (reduce (fn [mapped-template [key value]]
                     (assoc mapped-template (keyword key) value))
                   {}
                   raw-template))

(defn format-api-search-response
  [response]
  (map (fn [search-item]
         (let [raw-template (get search-item "template")
               converted-template (convert-keys-to-keywords raw-template)]
           (assoc converted-template :rank 0)))
       response))

(re-frame/reg-event-db
 :process-templates-reponse
 (fn [db [_ response]]
   (let [templates (format-api-search-response (js->clj response))
         memes (search "" templates)]
     (-> db
         (assoc :loading? false)
         (assoc :initialized? true)
         (assoc :filtered-meme-templates memes)
         (assoc :available-meme-templates templates)))))

(re-frame/reg-event-db
 :api-error
 (fn [db _]
   (-> db
       (assoc :had-error? true)
       (assoc :loading? false)
       (assoc :initialized? false))))

;;; Test row implementation
;; [{:row-index 0
;;   :memes [{
;;            :name "foo"
;;            :rank 1
;;            :selected true } ...]}
;;  {:row-index 1
;;   :selected true
;;   :meme {
;;            :name "foo"
;;            :rank 1
;;            :selected true }}]
