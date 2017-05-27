(ns memegen-ui.lib.api
  (:require [re-frame.core :as re-frame]
            [ajax.core :refer [GET]]
            [memegen-ui.config :as config]))

(defn meme-complete [top-text bottom-text]
  (GET
    (str "http://memecomplete.herokuapp.com/api/memes/?text=" top-text "/" bottom-text "&source=mightyfinememes&context=http://memegen.link/")
    {:handler #()
     :error-handler #()}))

(defn api-search-handler [response]
  (re-frame/dispatch [:memes-fetched response]))

(defn api-error-handler [{:keys [status status-text]}]
  (.log js/console (str "something bad happened: " status " " status-text))
  (re-frame/dispatch [:api-error]))

(defn fetch-memes
  "Fetches the memes from the memegen API"
  []
  (GET config/api-search-url
       {:handler api-search-handler
        :error-handler api-error-handler}))

(defn assoc-as-keyword [map [key value]]
  (assoc map (keyword key) value))

(defn convert-to-keywords [raw-template]
  (reduce assoc-as-keyword {} raw-template))

(defn format-api-search-item [search-item]
  (let [template (get search-item "template")]
    (assoc (convert-to-keywords template) :rank 0)))

(defn format-api-response
  "Formats the response from the memegen API into what we expect
  for the app"
  [response]
  (map format-api-search-item response))
