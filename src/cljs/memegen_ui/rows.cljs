(ns memegen-ui.rows)


(def sample-rows
  [{:row-index 0
    :memes [{:name "foo1"}
            {:name "bar1"}
            {:name "baz1"}]}
   {:row-index 1
    :memes [{:name "foo2"}
            {:name "bar2"}
            {:name "baz2"}]}
   {:row-index 2
    :memes [{:name "foo3"}
            {:name "bar3"}
            {:name "baz3"}]}])

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

(defn selected-row-index [rows]
  (:row-index
   (first
    (filter #(:selected %) rows))))

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
