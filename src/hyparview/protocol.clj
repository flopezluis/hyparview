(ns hyparview.protocol
  (:require
   [clojure.string :as str]
   [hyparview.manager :as manager]
   [clojure.tools.logging :as log]))


(defn drop-from-active-view [node]
  (prn node))

(defmulti process-message (fn [msg] (:type msg)))
(defmethod process-message :join
  [node]
  (let [id (:id node)]
    (log/debug (str "Reveived join from" node))
    (if (manager/is-there-a-slot-free? :active-view id)
      (drop-from-active-view id))
    (manager/add-to-active-view id)
    (manager/forward-join id)
    id))

(defmethod process-message :disconnect
  [node]
  (let [id (:id node)]
    (log/debug "disconnect")
    id))

(defmethod process-message :forward-join
  [msg]
  (log/debug "forward-join")
  (.get msg 1))

(defmethod process-message :neighbor-request
  [msg]
  (log/debug "neighbor")
  (.get msg 1))

(defmethod process-message :shuffle
  [msg]
  (log/debug "shuffle")
  (.get msg 1))

(defmethod process-message :shuffle-reply
  [msg]
  (log/debug "shuffle-reply")
  (.get msg 1))

(comment 
(defn send-join []
  (transport/send ))

(defn send-forward-join [to new-node sender]
 (transport/send :forward-join to new-node (:ARWL conf) sender)))
