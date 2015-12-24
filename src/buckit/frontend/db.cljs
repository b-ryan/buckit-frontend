(ns buckit.frontend.db
  (:require [buckit.frontend.models.core :as models]
            [buckit.frontend.utils       :as utils]))

;TODO prismatic schema to define db?

(def url-path                :url-path)
(def url-params              :url-params)
(def resources               :resources)

; queries represent GET requests to the backend database. They are a
; combination of resource type and optional ID and query parameters.
(def queries                 :queries)


(def initial-state
  {url-path                []
   url-params              {}
   resources               {}
   queries                 {}})


(defn get-resources
  [db model]
  {:pre [(models/valid-model? model)]}
  (get-in db [resources model] {}))

(defn inject-resources
  [db model objs]
  {:pre [(models/valid-model? model)
         (sequential? objs)]}
  (js/console.log (str (count objs) " object(s) being added to resource "
                       model))
  (update-in db [resources model]
             merge (utils/index-by-key models/id objs)))

(defn update-query
  [db query-id f & args]
  (update-in db [queries query-id] #(apply f % args)))
