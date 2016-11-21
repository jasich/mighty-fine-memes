(ns memegen-ui.views
  (:require [re-frame.core :refer [subscribe dispatch]]))


;;
;; Helpers
(defn row-key
  "Creates a unique React key for a tuple of components"
  [meme-tuple]
  (apply str (map :name meme-tuple)))


;;
;; Components
(defn meme-view [meme]
  [:div.col-xs-3.meme-listing-view
   [:div.meme-listing-view__meme
    [:span.meme-listing-view__meme__helper]
    [:img.meme-listing-view__meme__img {:src (:blank meme)
                                        :alt (:name meme)}]]
   [:div.meme-listing-view__name (:name meme)]])

(defn meme-listing []
  (let [meme-templates (subscribe [:filtered-meme-templates])]
    (fn []
      [:div.meme-listing
       (for [meme-tuple (partition 4 4 nil @meme-templates)]
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

(defn filter-bar []
  (let [filter-text (subscribe [:filter-text])]
    (fn []
      [:input.form-control {:type "text"
                            :placeholder "Filter memes by name"
                            :value @filter-text
                            :on-change #(dispatch [:filter-text (-> % .-target .-value)])}])))

(defn main-panel []
  [:div
   [:div.row
    [:div.col-xs-3
     [:span.brand "memegen.link"]]
    [:div.col-xs-9
     [filter-bar]]]
   [top-panel]])
