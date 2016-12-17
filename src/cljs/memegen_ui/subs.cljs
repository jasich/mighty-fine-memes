(ns memegen-ui.subs
    (:require-macros [reagent.ratom :refer [reaction]])
    (:require [re-frame.core :as re-frame]
              [memegen-ui.rows :as rows]))

(re-frame/reg-sub
 :initialized?
 (fn [db]
   (:initialized? db)))

(re-frame/reg-sub
 :had-error?
 (fn [db]
   (:had-error? db)))

(re-frame/reg-sub
 :available-meme-templates
 (fn [db]
   (:available-meme-templates db)))

(re-frame/reg-sub
 :filtered-meme-templates
 (fn [db]
   (:filtered-meme-templates db)))

(re-frame/reg-sub
 :filter-text
 (fn [db]
   (:filter-text db)))

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
