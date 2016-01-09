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
  [db model-type]
  {:pre [(models/valid-model-type? model-type)]}
  (get-in db [resources model-type] {}))

(defn inject-resources
  [db model-type objs]
  {:pre [(models/valid-model-type? model-type)
         (sequential? objs)]}
  (js/console.log (str (count objs) " object(s) being added to resource "
                       model-type))
  (update-in db [resources model-type]
             merge (utils/index-by-key models/id objs)))

(defn update-query
  [db query-id f & args]
  (update-in db [queries query-id] #(apply f % args)))
