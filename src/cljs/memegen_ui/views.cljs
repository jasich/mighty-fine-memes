(ns memegen-ui.views
  (:require [re-frame.core :refer [subscribe dispatch]]))

(defn meme-template-img [meme-link]
  (str (clojure.string/replace meme-link  "/api/templates" "") "/_/_.jpg"))

(defn meme-template-view [meme]
  (let [src (meme-template-img (:link meme))]
    [:div.col-xs-3
     [:div.meme-template
      [:span.helper]
      [:img {:src src
             :alt (:name meme)}]]
     [:div.meme-name (:name meme)]]))

(defn row-key [meme-tuple]
  (apply str (map :name meme-tuple)))

(defn meme-template-selector []
  (let [available-meme-templates (subscribe [:available-meme-templates])]
    (fn []
      [:div.available-memes
       (for [meme-tuple (partition 4 4 nil (sort-by :name @available-meme-templates))]
         ^{:key (row-key meme-tuple)} [:div.row
                                       (for [meme meme-tuple]
                                         ^{:key (:name meme)} [meme-template-view meme])])])))
(defn loading []
  [:div.initializing
   [:span "loading"]
   [:img {:src "images/ellipsis.gif"}]])

(defn top-panel []
  (let [ready? (subscribe [:initialized?])]
    (fn []
      (if-not @ready?
        [loading]
        [meme-template-selector]))))

(defn main-panel []
  [:div
   [:h2.title "memegen.link"]
   [top-panel]])
