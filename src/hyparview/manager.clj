(ns hyparview.manager
  (:require 
            [clojure.string :as str]
            [hyparview.transport :as transport]
            [clojure.tools.logging :as log]
            [overtone.at-at :as at]
            [hyparview.config :refer [conf load-conf]]))


(def my-pool (at/mk-pool))
(def active-members (ref {}))
(def passive-members (ref {}))
(def timers (ref {}))
(def me (atom ""))

(def config-file "dev/config.edn")
(load-conf config-file)

(defn forward-join [new-node]
  (doseq [node (vals @active-members)]
    (if (not= node new-node)
      (transport/send-forward-join node new-node @me))))

(defn view-full? [active-view node max]
  (and (not (contains? active-view (:id node)))
       (> (:max-active-view conf) (count active-view))))

(defn is-there-a-slot-free? [type node]
  (if (= type :active-view)
    (view-full? @active-members node (:max-active-view conf))
    (view-full? @passive-members node (:max-passive-view conf))))

(defn add-to-active-view [node]
  (dosync
   (alter active-members assoc (:id node) node)))

(defn generate-id [address port]
  (str address ":" port))

(defn select-random-members []
  "Returns N (fanout) members from the memberlist without me"
  (let [others (filter (fn [m] (not= @me (:id m)))  (vals @active-members))]
    (take (:fanout conf) (shuffle others))))

(defn increase-heartbeat []
  (dosync (alter active-members update-in [@me :heartbeat] inc)))
