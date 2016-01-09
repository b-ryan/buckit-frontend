(ns buckit.frontend.models.transaction)

(def id       :id)
(def date     :date)
(def payee-id :payee_id)
(def splits   :splits)

;; These properties are not always set on the transaction. But they can be used
;; when saving splits in order to save changes to the properties
;; transactionally.
(def payee    :payee)
