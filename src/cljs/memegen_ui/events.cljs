(ns memegen-ui.events
  (:require [re-frame.core :as re-frame]
            [memegen-ui.db :as db]
            [memegen-ui.lib.api :as api]
            [memegen-ui.lib.editor :as editor]
            [memegen-ui.lib.rows :as rows]
            [memegen-ui.lib.search :as search]))


;; ------------------------------------------------------------------------
;; Debounce logic
(defonce timeouts
  (atom {}))

(re-frame/reg-fx
 :dispatch-debounce
 (fn [[id event-vec n]]
   (js/clearTimeout (id @timeouts))
   (swap! timeouts assoc id
          (js/setTimeout (fn []
                           (re-frame/dispatch event-vec)
                           (swap! timeouts dissoc id))
                         n))))


;; ------------------------------------------------------------------------
;; Data
(re-frame/reg-event-db
 :initialize-db
 (fn  [_ _]
   db/default-db))

(re-frame/reg-event-db
 :get-init-data
 (fn [db _]
   (api/fetch-memes)
   (assoc db :loading? true)))

(re-frame/reg-event-db
 :memes-fetched
 (fn [db [_ response]]
   (let [memes (api/format-api-response (js->clj response))]
     (re-frame/dispatch [:do-search])
     (-> db
         (assoc :loading? false)
         (assoc :initialized? true)
         (assoc :memes memes)))))

(re-frame/reg-event-db
 :api-error
 (fn [db _]
   (-> db
       (assoc :had-error? true)
       (assoc :loading? false)
       (assoc :initialized? false))))


;; ------------------------------------------------------------------------
;; Search
(re-frame/reg-event-fx
 :debounced-search
 (fn [_ [_ search-text]]
   {:dispatch-debounce [::filter [:do-search] 250]}))

(re-frame/reg-event-db
 :do-search
 (fn [db [_ _]]
   (assoc db :meme-listing (search/search-memes (:search-text db)
                                                (:memes db)
                                                (:columns-per-row db)))))

(re-frame/reg-event-db
 :search-text-updated
 (fn [db [_ search-text]]
   (re-frame/dispatch [:debounced-search])
   (assoc db :search-text search-text)))


;; ------------------------------------------------------------------------
;; Editor
(re-frame/reg-event-fx
 :update-image
 (fn [_ _]
   {:dispatch-debounce [::editor [:set-image-url] 250]}))

(re-frame/reg-event-db
 :meme-selected
 (fn [db [_ meme]]
   (re-frame/dispatch [:update-image])
   (-> db
       (assoc :meme-listing (rows/insert-editor-row meme (:meme-listing db)))
       (assoc :meme-url (:blank meme)))))

(re-frame/reg-event-db
 :selection-closed
 (fn [db _]
   (-> db
       (assoc :meme-listing (rows/remove-selected-row (:meme-listing db)))
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
   (let [selected-row (rows/selected-row (:meme-listing db))]
     (-> db
         (assoc :meme-url (editor/create-meme (:meme selected-row)
                                              (:top-text db)
                                              (:bottom-text db)))
         (assoc :meme-updating false)))))


;; ------------------------------------------------------------------------
;; Window events
(re-frame/reg-event-fx
 :window-resizing
 (fn [_ _]
   {:dispatch-debounce [::window [:window-resized] 100]}))

(re-frame/reg-event-db
 :window-resized
 (fn [db _]
   (let [column-count (rows/columns-per-row (.-innerWidth js/window))]
     (if (not= column-count (:columns-per-row db))
       (do
         (re-frame/dispatch [:do-search])
         (assoc db :columns-per-row column-count))
       db))))
