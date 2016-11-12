(ns memegen-ui.views
  (:require [re-frame.core :refer [subscribe dispatch]]))


;;
;; Helpers
(defn meme-template-img
  "Creates an image link to a blank template image based on a meme templates URL"
  [meme-link]
  (str (clojure.string/replace meme-link  "/api/templates" "") "/_/_.jpg"))

(defn row-key
  "Creates a unique React key for a tuple of components"
  [meme-tuple]
  (apply str (map :name meme-tuple)))


;;
;; Components
(defn meme-view [meme]
  (let [src (meme-template-img (:link meme))]
    [:div.col-xs-3.meme-listing-view
     [:div.meme-listing-view__meme
      [:span.meme-listing-view__meme__helper]
      [:img.meme-listing-view__meme__img {:src src
             :alt (:name meme)}]]
     [:div.meme-listing-view__name (:name meme)]]))

(defn meme-listing []
  (let [meme-templates (subscribe [:available-meme-templates])]
    (fn []
      [:div.meme-listing
       (for [meme-tuple (partition 4 4 nil (sort-by :name @meme-templates))]
         ^{:key (row-key meme-tuple)} [:div.row.meme-listing__meme-tuple
                                       (for [meme meme-tuple]
                                         ^{:key (:name meme)} [meme-view meme])])])))
(defn loading-view []
  [:div.loading-view
   [:span.loading-view__message "loading"]
   [:img.loading-view__spinner {:src "images/ellipsis.gif"}]])

(defn top-panel []
  (let [ready? (subscribe [:initialized?])]
    (fn []
      (if-not @ready?
        [loading-view]
        [meme-listing]))))

(defn main-panel []
  [:div
   [:h2.brand "memegen.link"]
   [top-panel]])
