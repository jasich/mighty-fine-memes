(ns memegen-ui.filter
  (:require [clojure.string :as string]))

(defn count-keyword-matches
  "Counts how many times each word in the filter text appears in
   the keywords for a meme"
  [keywords filter-text]
  (let [filter-words (map string/lower-case (string/split filter-text #"\s"))
        lower-keywords (map string/lower-case keywords)]
    (reduce (fn [filter-count filter-word]
              (+ filter-count (reduce (fn [keyword-count keyword]
                           (if (string/includes? keyword filter-word)
                             (inc keyword-count)
                             keyword-count))
                         0
                         lower-keywords)))
            0
            filter-words)))

(defn rank-memes
  "Add a meme ranking to each meme based on the given filter text"
  [memes filter-text]
  (map (fn [meme]
         (if (empty? filter-text)
           (assoc meme :rank 0)
           (assoc meme :rank (count-keyword-matches (:keywords meme) filter-text))))
       memes))

(defn filter-memes
  "Filters memes given the filter-text"
  [available-memes filter-text]
  (let [text (string/lower-case filter-text)
        ranked-memes (rank-memes available-memes text)
        total-matches (reduce (fn [total item] (+ total (:rank item))) 0 ranked-memes)]
    (if (or (> total-matches 0) (not (empty? filter-text)))
      (filter #(> (:rank %) 0) ranked-memes)
      ranked-memes)))

(defn sort-memes [memes]
  "Sorts memes by :rank if ranked, else by :name"
  (if (some #(> (:rank %) 0) memes)
    (reverse (sort-by :rank (sort-by :name memes)))
    (sort-by :name memes)))
