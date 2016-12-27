(ns memegen-ui.events
  (:require [re-frame.core :as re-frame]
            [clojure.string :as str]
            [ajax.core :refer [GET]]
            [memegen-ui.config :as config]
            [memegen-ui.db :as db]
            [memegen-ui.search :refer [search]]
            [memegen-ui.rows :as rows]
            [memegen-ui.editor :as editor]))

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

(re-frame/reg-event-fx
 :update-image
 (fn [_ _]
   {:dispatch-debounce [::editor [:set-image-url] 250]}))

(re-frame/reg-event-fx
 :window-resizing
 (fn [_ _]
   {:dispatch-debounce [::window [:window-resized] 100]}))

;; db events
(re-frame/reg-event-db
 :initialize-db
 (fn  [_ _]
   db/default-db))

(re-frame/reg-event-db
 :filter-memes
  (fn [db [_ filter-text]]
    (let [available-memes (:available-meme-templates db)
          columns-per-row (:columns-per-row db)
          filtered-memes (search filter-text available-memes columns-per-row)]
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

(defn assoc-as-keyword [map [key value]]
  (assoc map (keyword key) value))

(defn convert-to-keywords [raw-template]
  (reduce assoc-as-keyword {} raw-template))

(defn format-api-search-item [search-item]
  (let [template (get search-item "template")]
    (assoc (convert-to-keywords template) :rank 0)))

(defn format-api-search-response [response]
  (map format-api-search-item response))

(re-frame/reg-event-db
 :process-templates-reponse
 (fn [db [_ response]]
   (let [templates (format-api-search-response (js->clj response))
         columns-per-row (:columns-per-row db)
         memes (search "" templates columns-per-row)]
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

(re-frame/reg-event-db
 :meme-selected
 (fn [db [_ meme]]
   (let [memes (vec (rows/remove-selected-row (:filtered-meme-templates db)))
         clicked-index (rows/row-index-of-meme meme memes)
         new-row-index (inc clicked-index)
         new-row (rows/create-selected-row new-row-index meme)]
     (re-frame/dispatch [:update-image])
     (-> db
         (assoc :filtered-meme-templates
                (rows/insert-row new-row new-row-index memes))
         (assoc :meme-url (:blank meme))))))

(re-frame/reg-event-db
 :selection-closed
 (fn [db _]
   (-> db
       (assoc :filtered-meme-templates (rows/remove-selected-row (:filtered-meme-templates db)))
       (assoc :top-text "")
       (assoc :bottom-text ""))))


(re-frame/reg-event-db
 :top-text-updated
 (fn [db [_ top-text]]
   (re-frame/dispatch [:update-image])
   (-> db
       (assoc :top-text top-text)
       (assoc :meme-updating true))))

(re-frame/reg-event-db
 :bottom-text-updated
 (fn [db [_ bottom-text]]
   (re-frame/dispatch [:update-image])
   (-> db
       (assoc :bottom-text bottom-text)
       (assoc :meme-updating true))))


(re-frame/reg-event-db
 :set-image-url
 (fn [db _]
   (let [selected-row (rows/selected-row (:filtered-meme-templates db))]
     (-> db
         (assoc :meme-url (editor/create-meme (:meme selected-row) (:top-text db) (:bottom-text db)))
         (assoc :meme-updating false)))))


(re-frame/reg-event-db
 :window-resized
 (fn [db _]
   (let [column-count (rows/columns-per-row (.-innerWidth js/window))
         filter-text (:filter-text db)
         available-memes (:available-meme-templates db)]
     (if (not= column-count (:columns-per-row db))
       (-> db
           (assoc :columns-per-row column-count)
           (assoc :filtered-meme-templates (search filter-text available-memes column-count)))
       db))))
