(ns memegen-ui.db)

(def default-db
  {:initialized? false
   :had-error? false
   :search-text ""
   :memes []
   :meme-listing []
   :top-text ""
   :bottom-text ""
   :columns-per-row 4
   :meme-updating false
   :meme-url nil
   :editor-message ""})
