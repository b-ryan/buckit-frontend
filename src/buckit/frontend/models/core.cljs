(ns buckit.frontend.models.core
  (:require [buckit.frontend.models.split :as models.split]
            [buckit.frontend.models.transaction :as models.transaction]))

(def id
  "All objects use :id as the primary key for the objects. This can be used
  when dealing with objects that might be a variety of types (like it might be
  an account or it might be a transaction). Generally it is preferable to use
  the id defined in the particular model you are dealing with."
  :id)

(defn account-in-splits?
  "Returns true if any of the splits for the given transaction have the account
  ID."
  [account-id transaction]
  {:pre [(integer? account-id)]}
  (let [splits      (models.transaction/splits transaction)
        account-ids (map models.split/account-id splits)]
    (some #{account-id} account-ids)))