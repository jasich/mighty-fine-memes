(ns memegen-ui.editor
  (:require [clojure.string :as str]))


(defn clean-text
  [text]
  (let [replaced-text (str/replace (str/replace text " " "_") "?" "~q")]
    (or (and (not (empty? replaced-text)) replaced-text)
        "_")))

(defn create-meme
  [selected-meme top-text bottom-text]
  (let [base-url (str/replace (:blank selected-meme) "_.jpg" "")]
    (str base-url (clean-text top-text) "/" (clean-text bottom-text) ".jpg")))
