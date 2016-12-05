(ns memegen-ui.views
  (:require [re-frame.core :refer [subscribe dispatch]]))


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
  (let [meme-rows (subscribe [:filtered-meme-templates])]
    (fn []
      [:div.meme-listing
       (for [meme-row @meme-rows]
         ^{:key (str "row-" (:row-index meme-row))}
         [:div.row.meme-listing__meme-tuple
          (for [meme (:memes meme-row)]
            ^{:key (:name meme)} [meme-view meme])])])))

(defn loading-view []
  [:div.loading-view
   [:span.loading-view__message "loading"]
   [:img.loading-view__spinner {:src "images/ellipsis.gif"}]])

(defn error-view []
  [:div.error-view
   [:span.error-view__message "An error has occurred :("]])

(defn top-panel []
  (let [ready? (subscribe [:initialized?])
        had-error? (subscribe [:had-error?])]
    (fn []
      (if @had-error?
        [error-view]
        (if-not @ready?
          [loading-view]
          [meme-listing])))))

(defn filter-bar []
  (let [filter-text (subscribe [:filter-text])]
    (fn []
      [:input.form-control {:type "text"
                            :placeholder "Filter memes by name"
                            :value @filter-text
                            :on-change #(dispatch [:filter-text-updated (-> % .-target .-value)])}])))

(defn main-panel []
  [:div
   [:div.row
    [:div.col-xs-3
     [:span.brand "memegen.link"]]
    [:div.col-xs-9
     [filter-bar]]]
   [top-panel]])
