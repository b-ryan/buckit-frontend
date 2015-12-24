(ns buckit.frontend.db
  (:require [buckit.frontend.models.core :as models]
            [buckit.frontend.utils       :as utils]))

;TODO prismatic schema to define db?

(def url-path                :url-path)
(def url-params              :url-params)
(def resources               :resources)
(def pending-initializations :pending-initializations)

; queries represent GET requests to the backend database. They are a
; combination of resource type and optional ID and query parameters.
(def queries                 :queries)


(def initial-state
  {url-path                []
   url-params              {}
   resources               {models/accounts     {}
                            models/payees       {}
                            models/transactions {}}
   pending-initializations #{models/accounts models/payees}
   queries                 {}})


(defn get-resource
  [db resource]
  (get-in db [resources resource]))

(defn inject-resources
  [db resource objs]
  (js/console.log (str (count objs)
                       " object(s) being added to resource "
                       resource))
  (update-in db [resources resource]
             merge (utils/index-by-key models/id objs)))

(defn complete-initialization
  [db resource]
  (update-in db [pending-initializations] disj resource))

(defn update-query
  [db query f & args]
  (update-in db [queries query] #(apply f % args)))
