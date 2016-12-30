(ns memegen-ui.lib.editor
  (:require [clojure.string :as str]))


(defn clean-text
  "Replaces characters in the given text with what the memegen API expects"
  [text]
  (let [replaced-text (str/replace (str/replace text " " "_") "?" "~q")]
    (or (and (not (empty? replaced-text)) replaced-text)
        "_")))

(defn create-meme
  "Creates the meme url given a meme & text"
  [selected-meme top-text bottom-text]
  (let [base-url (str/replace (:blank selected-meme) "_.jpg" "")]
    (str base-url (clean-text top-text) "/" (clean-text bottom-text) ".jpg")))
