(ns memegen-ui.views
  (:require [re-frame.core :refer [subscribe dispatch]]))

(defn meme-template-view
  [meme]
  (let [src (str (clojure.string/replace (:link meme) "/api/templates" "") "/_/_.jpg")]
    [:div.col-xs-3
     [:div.meme-template
      [:span.helper]
      [:img {:src src
             :alt (:text meme)}]]]))

(defn meme-template-selector
  []
  (let [available-meme-templates (subscribe [:available-meme-templates])]
    (fn []
      [:div.available-memes
       [:h4 "available memes"]
       (for [meme-tuple (partition 4 4 nil @available-meme-templates)]
         [:div.row
          (for [meme meme-tuple]
            ^{:key (:text meme)} [meme-template-view meme])])])))

(defn top-panel
  []
  (let [ready? (subscribe [:initialized?])]
    (fn []
      (if-not @ready?
        [:div.initializing
         [:span "loading"]
         [:img {:src "images/ellipsis.gif"}]]
        [meme-template-selector]))))

(defn main-panel []
  [:div
   [:h2.title "memegen.link"]
   [top-panel]])
