(ns memegen-ui.views
  (:require [re-frame.core :refer [subscribe dispatch]]
            [reagent.core :as reagent]
            [memegen-ui.lib.ux :as ux]))


;;
;; Components
(defn meme-view [meme]
  [:div {:id (:name meme)
         :class-name "col-md-3 col-xs-12 col-sm-6 meme-listing-view"
         :on-click #(dispatch [:meme-selected meme]) }
   [:div.meme-listing-view__meme
    [:span.meme-listing-view__meme__helper]
    [:img.meme-listing-view__meme__img {:src (:blank meme)
                                        :alt (:name meme)}]]
   [:div.meme-listing-view__name (:name meme)]])

(defn meme-editor
  [meme]
  (reagent/create-class
   {:component-did-mount
    #(ux/scroll-to-id "meme-editor")

    :display-name "meme-editor"

    :reagent-render
    (fn [meme]
      (let [top-text (subscribe [:top-text])
            bottom-text (subscribe [:bottom-text])
            meme-url (subscribe [:meme-url])
            meme-updating (subscribe [:meme-updating])
            editor-message (subscribe [:editor-message])]
        (fn []
          (let [updating-meme @meme-updating
                message @editor-message]
            [:div.row.meme-listing__selected-row {:id "selected-row"}
             [:div.meme-editor {:id "meme-editor"}
              [:h3 (:name meme)]
              [:div.row
               [:div.col-sm-6
                [:div.meme-editor__view
                 [:span.meme-editor__view__helper]
                 [:a {:href @meme-url
                      :target "_blank"}
                  [:img.meme-editor__view__image {:src @meme-url
                                                  :alt (:name meme)}]]]]
               [:div.col-sm-6
                [:div.form-group
                 [:label "Top Text"]
                 [:input.form-control {:id "top-text"
                                       :value @top-text
                                       :autoFocus true
                                       :on-change #(dispatch [:top-text-updated (-> % .-target .-value)])}]]
                [:div.form-group
                 [:label "Bottom Text"]
                 [:input.form-control {:id "bottom-text"
                                       :value @bottom-text
                                       :on-change #(dispatch [:bottom-text-updated (-> % .-target .-value)])}]]
                [:div.form-group
                 [:input.form-control {:id "meme-url"
                                       :value @meme-url
                                       :readOnly true}]]

                [:div.meme-editor__actions.form-group.clearfix
                 [:button.btn.btn-primary {:type "button"
                                           :on-click #(dispatch [:copy-meme-url])} "Copy Link"]
                 [:button.btn.btn-default.pull-right {:type "button"
                                                      :on-click #(dispatch [:selection-closed])} "Done"]]

                [:div.meme-editor__message message]

                (if updating-meme
                  [:div.meme-editor__view__updating
                   [:img {:src "images/ellipsis.gif"}]
                   [:div.meme-editor__view__updating__text "updating..."]])]]]]))))}))

(defn meme-tuple [meme-row]
  [:div.row.meme-listing__meme-tuple
   (for [meme (:memes meme-row)]
     ^{:key (:name meme)} [meme-view meme])])

(defn meme-listing []
  (let [meme-rows (subscribe [:meme-listing])]
    (fn []
      [:div
       [:div.row
        [:div.col-xs-12
         [:h4.cta "Select Your Meme"]]]
       [:div.meme-listing.row
        [:div.col-xs-12
         (for [meme-row @meme-rows]
           (if (:selected meme-row)
             ^{:key (str "row-" (:row-index meme-row))}[meme-editor (:meme meme-row)]
             ^{:key (str "row-" (:row-index meme-row))}[meme-tuple meme-row]))]]])))

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
  (let [filter-text (subscribe [:search-text])]
    (fn []
      [:input.form-control {:type "text"
                            :placeholder "Filter memes by name"
                            :value @filter-text
                            :autoFocus true
                            :on-change #(dispatch [:search-text-updated (-> % .-target .-value)])}])))

(defn main-panel []
  [:div
   [:div.row
    [:div.col-sm-4.branding
     [:span.brand "Mighty Fine Memes"]
     [:span.brand-sub
      [:span "powered by "]
      [:a {:href "https://memegen.link/"
           :target "_blank"} "memegen.link"]]]
    [:div.col-sm-8
     [filter-bar]]]
   [top-panel]])
