(ns memegen-ui.events
  (:require [re-frame.core :as re-frame]
            [clojure.string :as str]
            [memegen-ui.db :as db]
            [memegen-ui.lib.api :as api]
            [memegen-ui.lib.editor :as editor]
            [memegen-ui.lib.rows :as rows]
            [memegen-ui.lib.search :as search]
            [memegen-ui.lib.ux :as ux]
            [dandy-roll.core :as dr]
            [dandy-roll.draw :as draw]
            [dandy-roll.canvas :refer [data-url]]
            [dandy-roll.promise :refer [then]]))


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
   (re-frame/dispatch [:update-meme-image])
   (-> db
       (assoc :top-text top-text)
       ;; (assoc :meme-updating true)
       )))

(re-frame/reg-event-db
 :bottom-text-updated
 (fn [db [_ bottom-text]]
   (re-frame/dispatch [:update-meme-image])
   (-> db
       (assoc :bottom-text bottom-text)
       ;; (assoc :meme-updating true)
       )))


(re-frame/reg-event-db
 :set-image-url
 (fn [db _]
   (let [selected-row (rows/selected-row (:meme-listing db))]
     (-> db
         (assoc :meme-url (editor/create-meme (:meme selected-row)
                                              (:top-text db)
                                              (:bottom-text db)))
         (assoc :meme-updating false)))))

(re-frame/reg-event-db
 :copy-meme-url
 (fn [db [_ copy-status]]
   (re-frame/dispatch [:dismiss-editor-message])
   (assoc db :editor-message copy-status)))

(re-frame/reg-event-fx
 :dismiss-editor-message
 (fn [_ _]
   {:dispatch-debounce [::editor [:clear-editor-message] 2000]}))


(re-frame/reg-event-db
 :clear-editor-message
 (fn [db _]
   (assoc db :editor-message "")))

(def sizes {:big {:count 10
                  :size 100}
            :big1 {:count 20
                   :size 55}
            :med {:count 30
                  :size 35}
            :small1 {:count 40
                     :size 27}
            :small{:count 50
                   :size 25}})

(def rate-of-change (/ (- (get-in sizes [:small :size]) (get-in sizes [:big :size]))
                       (- (get-in sizes [:small :count]) (get-in sizes [:big :count]))))
(def y-intercept (- (get-in sizes [:big :size]) (* rate-of-change (get-in sizes [:big :count]))))

(def magic-number 1031.125447)
;; 100 = rate-of-change * 10 + b

(defn get-font-size [char-count]
  (let [suggested-size (+ 3 (/ 1171.037212 (+ char-count 1.701793604)))]
    (if (> suggested-size 100)
      100
      suggested-size)))

(defn center-x [canvas drawable]
  (/ (- (.-width canvas) (draw/width drawable canvas)) 2))

(defn needs-break [words]
  (let [word-count (count words)]
    (> word-count 5)))

(defn pick-a-size [top-text bottom-text]
  (let [top-count (count top-text)
        bot-count (count bottom-text)]
    (if (> top-count bot-count)
      (int (get-font-size top-count))
      (int (get-font-size bot-count)))))


(re-frame/reg-event-db
 :update-meme-image
 (fn [db _]
   (let [meme-url (:meme-url db)
         top-text (str/upper-case (:top-text db))
         top-text-words (str/split top-text #" ")
         top-text-break? (needs-break top-text-words)
         bottom-text (str/upper-case (:bottom-text db))
         stroked-top (dr/center-top {:strokeStyle "black" :lineWidth 2})
         stroked-bottom (dr/center-bottom {:strokeStyle "black" :lineWidth 2})
         top-font-size (int (get-font-size (count top-text)))
         second-line (dr/drawer center-x #(identity (+ 30 top-font-size)))]
     (if top-text-break?
       (let [break-point (.round js/Math (/ (count top-text-words) 2))
             top-line (str/join " " (take break-point top-text-words))
             bot-line (str/join " " (drop break-point top-text-words))
             font-size (pick-a-size top-line bot-line)]
         (dr/watermark meme-url
                       (dr/with-text top-line font-size "Impact" "#fff" stroked-top)
                       (dr/with-text bot-line font-size "Impact" "#fff"
                         (second-line {:strokeStyle "black" :lineWidth 2}))
                       (dr/with-text bottom-text 27 "Impact" "#fff" stroked-bottom)
                       (dr/to-data-url #(re-frame/dispatch [:update-meme-url %]))))
       (dr/watermark meme-url
                       (dr/with-text top-text top-font-size "Impact" "#fff" stroked-top)
                       (dr/with-text bottom-text 27 "Impact" "#fff" stroked-bottom)
                       (dr/to-data-url #(re-frame/dispatch [:update-meme-url %])))))
     db))


(re-frame/reg-event-db
 :update-meme-url
 (fn [db [_ url]]
   (assoc db :meme-data-url url)))


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
