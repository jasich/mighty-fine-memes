(ns memegen-ui.db)

(def default-db
  {:initialized? false
   :had-error? false
   :filter-text ""
   :available-meme-templates []
   :filtered-meme-templates []
   :top-text ""
   :bottom-text ""
   :window-width 0
   :window-height 0
   :columns-per-row 4
   :meme-url nil})
