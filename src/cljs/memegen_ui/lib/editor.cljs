(ns memegen-ui.lib.editor
  (:require [clojure.string :as str]))


(def replacement-chars {"?" "~q"
                        "%" "~p"
                        "#" "~h"
                        "/" "~s"
                        "''" "\""
                        "_" "__"
                        "-" "--"
                        " " "_"})

(defn sanitize-characters [user-input]
  (reduce (fn [text [a b]] (str/replace text a b))
          user-input
          replacement-chars))

(defn clean-text
  "Replaces characters in the given text with what the memegen API expects"
  [text]
  (let [replaced-text (sanitize-characters text)]
    (or (and (not (empty? replaced-text)) replaced-text)
        "_")))

(defn create-meme
  "Creates the meme url given a meme & text"
  [selected-meme top-text bottom-text]
  (let [base-url (str/replace (:blank selected-meme) "_.jpg" "")]
    (str base-url (clean-text top-text) "/" (clean-text bottom-text) ".jpg")))
