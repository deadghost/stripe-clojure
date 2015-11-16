(ns stripe-clojure.core
  "Functions for Stripe Customers API"
  (:require [clj-http.client :as client])
  (:refer-clojure :exclude (list)))

(def stripe-tokens
  "Your stripe public and private keys are stored here."
  (atom {:public "" :private ""}))

(defn set-tokens!
  "Set your stripe tokens.
  Ex:
  (set-tokens! {:private \"sk_test_xxxxxxxxxxxxxxxxxxxxxxxx\"})"
  [m] (swap! stripe-tokens (fn [a] (merge a m))))

(def stripe-api-url
  "Stripe API endpoint."
  "https://api.stripe.com/v1/")

(defn build-url [[url-ks url-vs] params]
  (str stripe-api-url
       (->> (map params url-vs)
            (interleave url-ks)
            (filter (comp not nil?))
            (interpose "/")
            (apply str))))

(defn make-request
  [params method resource]
  (:body (method (build-url resource params)
           {:basic-auth [(:private @stripe-tokens)]
            :query-params (apply dissoc params (second resource))
            :throw-exceptions false
            :as :json
            :coerce :always})))

(def url-vals {"cards" :card_id
               "charges" :charge_id
               ;;"coupons" :coupon_id
               "customers" :customer_id
               "events" :event_id
               ;;"invoiceitems" :invoiceitem_id
               "invoices" :invoice_id
               "plans" :plan_id
               "subscriptions" :subscription_id
               "tokens" :token_id})

;; resources lacking test coverage are commented out
;; second vector values can also be nil
(def url-mapping (into {}
                   (map (fn [[k v]] [k [v (mapv url-vals v)]])
                     {:cards ["customers" "cards"]
                      :charges ["charges"]
                      ;;:capture ["charges" "capture"] ; no-id endpt
                      ;;:refund ["charges" "refund"] ; no-id endpt
                      ;;:coupons ["coupons"]
                      :customers ["customers"]
                      ;;:discounts ["customers" "discount"] ; no-id endpt
                      :events ["events"]
                      ;;:invoiceitems ["invoiceitems"]
                      :invoices ["invoices"]
                      ;;:lines ["invoices" "lines"] ; no-id endpt
                      ;;:pay ["invoices" "pay"] ; no id endpt
                      ;;:upcoming ["invoices" "upcoming"] ; no-id endpt
                      :plans ["plans"]
                      :subscriptions ["customers" "subscriptions"]
                      :tokens ["tokens"]})))

(defmacro defop
  "Creates function that accepts a map with a resource keyword to a map of 
  params.
  Ex. {:customers {:customer_id \"mystripecustidhere\"}
                   :plan \"myplanidhere\"}"
  [op-name http-action]
  `(defn ~op-name [resource-map#]
     (let [[kw# params#] (first resource-map#)]
       (make-request params# ~http-action (url-mapping kw#)))))

;; operations lacking test coverage are commented out
(defop cancel client/delete)
#_(defop capture client/post)
(defop create client/post)
(defop delete client/delete)
(defop retrieve client/get)
(defop list client/get)
#_(defop pay client/post)
#_(defop refund client/post)
(defop update client/post)
