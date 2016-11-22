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
