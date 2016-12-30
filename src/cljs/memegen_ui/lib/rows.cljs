(ns memegen-ui.lib.rows)


(defn insert-row [new-row index rows]
  (concat (take index rows)
          [new-row]
          (map (fn [row]
                 (assoc row :row-index (inc (:row-index row))))
               (drop index rows))))

(defn remove-row [index rows]
  (if index
    (concat (take index rows)
            (map (fn [row]
                   (assoc row :row-index (dec (:row-index row))))
                 (drop (inc index) rows)))
    rows))

(defn selected-row [rows]
  (first
    (filter #(:selected %) rows)))

(defn selected-row-index [rows]
  (:row-index (selected-row rows)))

(defn remove-selected-row [rows]
  (remove-row (selected-row-index rows) rows))

(defn row-index-of-meme
  [meme rows]
  (:row-index
   (first
    (filter (fn [row]
              (some #(= meme %) (:memes row)))
            rows))))

(defn create-selected-row [index meme]
  (assoc {} :row-index index :selected true :meme meme))

(defn insert-editor-row
  "Inserts an editor row for the given meme into the meme-listing"
  [meme meme-listing]
  (let [memes (vec (remove-selected-row meme-listing))
        clicked-index (row-index-of-meme meme memes)
        new-row-index (inc clicked-index)
        new-row (create-selected-row new-row-index meme)]
    (insert-row new-row new-row-index memes)))

(defn columns-per-row
  "Determines the number of columns per row based on screen width"
  [width]
  (cond
    (< width 768) 1
    (< width 992) 2
    :else 4))

(defn rowify
  "Places the memes into row structures"
  [columns-per-row memes]
  (let [tuples (partition columns-per-row columns-per-row nil memes)]
    (map-indexed (fn [index item]
                   (assoc {} :row-index index :memes item))
                 tuples)))
