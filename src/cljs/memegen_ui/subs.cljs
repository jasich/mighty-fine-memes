(ns memegen-ui.subs
    (:require-macros [reagent.ratom :refer [reaction]])
    (:require [re-frame.core :as re-frame]))

(re-frame/reg-sub
 :initialized?
 (fn [db]
   (:initialized? db)))

(re-frame/reg-sub
 :available-meme-templates
 (fn [db]
   (:available-meme-templates db)))

(re-frame/reg-sub
 :top-text
 (fn [db]
   (:top-text db)))

(re-frame/reg-sub
 :bottom-text
 (fn [db]
   (:bottom-text db)))

(re-frame/reg-sub
 :selected-meme-template
 (fn [db]
   (:selected-meme-template db)))

(re-frame/reg-sub
 :rendered-meme-link
 (fn [db]
   (if-not (empty? (:selected-meme-template db))
     (str (:link (:selected-meme-template db)) "/" (:top-text db) "/" (:bottom-text db)))))
