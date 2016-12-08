(ns memegen-ui.views
  (:require [re-frame.core :refer [subscribe dispatch]]))


;;
;; Components
(defn meme-view [meme]
  [:div.col-xs-3.meme-listing-view { :on-click #(dispatch [:meme-selected meme]) }
   [:div.meme-listing-view__meme
    [:span.meme-listing-view__meme__helper]
    [:img.meme-listing-view__meme__img {:src (:blank meme)
                                        :alt (:name meme)}]]
   [:div.meme-listing-view__name (:name meme)]])

(defn meme-editor [meme]
  [:div.meme-editor
   [:div.col-xs-5
    [:div.meme-editor__view
     [:span.meme-editor__view__helper]
     [:img.meme-editor__view__image {:src (:blank meme)
                                                     :alt (:name meme)}]]]
   [:div.col-xs-7
    [:form
     [:div.form-group
      [:label "Top Text"]
      [:input.form-control]]
     [:div.form-group
      [:label "Bottom Text"]
      [:input.form-control]]
     [:button.btn.btn-default "Do It!"]]]])

(defn meme-row-view [meme-row]
  (if (:selected meme-row)
    [:div.row.meme-listing__selected-row
     [:div.pull-right.close {:on-click #(dispatch [:selection-closed])} "x"]
     [meme-editor (:meme meme-row)]]
    ^{:key (str "row-" (:row-index meme-row))}
    [:div.row.meme-listing__meme-tuple
     (for [meme (:memes meme-row)]
       ^{:key (:name meme)} [meme-view meme])]))

(defn meme-listing []
  (let [meme-rows (subscribe [:filtered-meme-templates])]
    (fn []
      [:div.meme-listing
       (for [meme-row @meme-rows]
         (meme-row-view meme-row))])))

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
