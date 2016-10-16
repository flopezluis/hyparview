(ns hyparview.manager
  (:require 
            [clojure.string :as str]
            [hyparview.transport :as transport]
            [clojure.tools.logging :as log]
            [overtone.at-at :as at]
            [hyparview.config :refer [conf load-conf]]))

(def my-pool (at/mk-pool))
(def active-members (ref []))
(def passive-members (ref []))
(def timers (ref {}))
(def me (atom ""))

(def config-file "dev/config.edn")
(load-conf config-file)

(defn is-empty? [view]
  (if (= type :view)
    (empty? @active-members)
    (empty? @passive-members)))

(defn view-full? [view node max]
  (or (some #(= node %) view)
       (< max (count view))))

(defn is-there-a-slot-free? [type node]
  (if (= type :active-view)
    (not (view-full? @active-members node (:max-active-view conf)))
    (not (view-full? @passive-members node (:max-passive-view conf)))))

(defn generate-id [address port]
  (str address ":" port))

(defn select-random-member [to-discard]
  "Returns N (fanout) members from the memberlist without me"
  (let [others (filter (fn [m] (not (contains? to-discard m)))  (vals @active-members))]
    (.get (shuffle others) 0)))

(defn forward-join-to-all-nodes [new-node]
  (doseq [node @active-members]
    (if (not= node new-node)
      (do
        (log/debug (str "forwarding " new-node " to " node ))
        (transport/send-forward-join node new-node @me)))))

(defn drop-from-active-view [node]
  (prn (str "drop " node)))

(defn add-to-passive-view [node]
  (prn (str "passive " node)))

(defn add-to-active-view [msg]
  (let [id (:new-node (:data msg))]
    (log/debug (str "Adding node to active view: " id))
    (if (not (is-there-a-slot-free? :active-view id))
      (drop-from-active-view id))
    (dosync (alter active-members conj id))))

(defmulti process-message (fn [data] (:type data)))
(defmethod process-message :join
  [data]
  (let [id (:new-node (:data data))]
    (log/debug (str "Reveived join from" data " id " id))
    (add-to-active-view data)
    (forward-join-to-all-nodes id)
    id))

(defmethod process-message :forward-join
  [data]
  (log/debug (str "forward-join--> " data))
  (if (or (= (:arwl data) 0) (is-empty? :active-view))
    (add-to-active-view data)
    (do
      (if (= :arwl data) (:PRWL conf)
          (add-to-passive-view data))
      (let [node (select-random-member [(:sender data)])]
        (transport/send-forward-join node (:new-node data) @me (- (:arwl data) 1))))))

(defmethod process-message :disconnect
  [msg]
  (let [id (:new-node (:data msg))]
    (log/debug "disconnect")
    id))

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
