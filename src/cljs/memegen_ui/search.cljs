(ns memegen-ui.search
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
  [filter-text memes]
  (map (fn [meme]
         (if (empty? filter-text)
           (assoc meme :rank 0)
           (assoc meme :rank (count-keyword-matches (:keywords meme) filter-text))))
       memes))

(defn sort-memes
  "Sorts memes by :rank if ranked, else by :name"
  [memes]
  (if (some #(> (:rank %) 0) memes)
    (reverse (sort-by :rank (sort-by :name memes)))
    (sort-by :name memes)))

(defn rowify
  "Places the memes into row structures"
  [columns-per-row memes]
  (let [tuples (partition columns-per-row columns-per-row nil memes)]
    (map-indexed (fn [index item]
                   (assoc {} :row-index index :memes item))
                 tuples)))

(defn filter-by-rank
  "Filters memes given :rank"
  [memes]
  (filter #(> (:rank %) 0) memes))

(defn search
  [filter-text memes columns-per-row]
  (let [text (string/lower-case filter-text)
        filter-memes (if (empty? text) identity filter-by-rank)]
    (->> memes
         (rank-memes text)
         (filter-memes)
         (sort-memes)
         (rowify columns-per-row))))
