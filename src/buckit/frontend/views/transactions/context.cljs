(ns buckit.frontend.views.transactions.context
  "context map:

  :account-id              ID of the account currently being worked on
                           (required)

  :selected-transaction-id ID of the transaction highlighted or being edited
                           (optional -- default: nil)

  :edit?                   Indicaes whether edit mode will be used.
                           (optional -- default: false)") 

(defn mode
  [context]
  (let [account-id (:account-id context)]
    (cond
      ; FIXME allow for multiple accounts?
      (nil? account-id) :no-account
      :else             :single-account)))

(defn is-selected?
  [context transaction-id]
  (= (:selected-transaction-id context) transaction-id))
