(ns memegen-ui.lib.search
  (:require [clojure.string :as str]
            [memegen-ui.lib.rows :as rows]))


(defn count-for-keyword
  "Increments count if the filter-term appears in the keyword"
  [filter-term cur-count keyword]
  (if (str/includes? keyword filter-term)
    (inc cur-count)
    cur-count))

(defn sum-keyword-matches-for-term
  "Sums the number of times a filter-term matches a keyword in keywords"
  [keywords cur-count filter-term]
  (let [count-keyword-fn (partial count-for-keyword filter-term)
        term-in-keywords-count (reduce count-keyword-fn 0 keywords)]
    (+ cur-count term-in-keywords-count)))

(defn count-keyword-matches
  "Counts how many times each word in the filter text appears in
   the keywords for a meme"
  [meme-keywords filter-text]
  (let [filter-terms (map str/lower-case (str/split filter-text #"\s"))
        keywords (map str/lower-case meme-keywords)]
    (reduce (partial sum-keyword-matches-for-term keywords) 0 filter-terms)))

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

(defn filter-by-rank
  "Filters memes given :rank"
  [memes]
  (filter #(> (:rank %) 0) memes))

(defn search-memes
  [filter-text memes columns-per-row]
  (let [text (str/lower-case filter-text)
        filter-memes (if (empty? text) identity filter-by-rank)]
    (->> memes
         (rank-memes text)
         (filter-memes)
         (sort-memes)
         (rows/rowify columns-per-row))))
