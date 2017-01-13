(ns memegen-ui.subs
    (:require-macros [reagent.ratom :refer [reaction]])
    (:require [re-frame.core :as re-frame]))

(re-frame/reg-sub
 :initialized?
 (fn [db]
   (:initialized? db)))

(re-frame/reg-sub
 :had-error?
 (fn [db]
   (:had-error? db)))

(re-frame/reg-sub
 :memes
 (fn [db]
   (:memes db)))

(re-frame/reg-sub
 :meme-listing
 (fn [db]
   (:meme-listing db)))

(re-frame/reg-sub
 :search-text
 (fn [db]
   (:search-text db)))

(re-frame/reg-sub
 :top-text
 (fn [db]
   (:top-text db)))

(re-frame/reg-sub
 :bottom-text
 (fn [db]
   (:bottom-text db)))

(re-frame/reg-sub
 :meme-url
 (fn [db]
   (:meme-url db)))

(re-frame/reg-sub
 :meme-updating
 (fn [db]
   (:meme-updating db)))

(re-frame/reg-sub
 :editor-message
 (fn [db]
   (:editor-message db)))


(re-frame/reg-sub
 :meme-data-url
 (fn [db]
   (if (empty? (:meme-data-url db))
     (:meme-url db)
     (:meme-data-url db))))
