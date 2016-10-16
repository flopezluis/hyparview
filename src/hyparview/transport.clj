(ns hyparview.transport
  (:require
            [aleph.tcp :as tcp]
            [gloss.core :as gloss]
            [gloss.io :as io]
            [clojure.edn :as edn]
            [manifold.deferred :as d]
            [manifold.stream :as s]
            [clojure.string :as str]
            [clojure.tools.logging :as log]))

(def protocol
  (gloss/compile-frame
   (gloss/finite-frame :uint32
                       (gloss/string :utf-8 :delimiters ["\r" "\r\n"]))
   pr-str
   edn/read-string))

(defn wrap-duplex-stream
  [protocol s]
  (let [out (s/stream)]
    (s/connect
     (s/map #(io/encode protocol %) out)
     s)
    (s/splice
     out
     (io/decode-stream s protocol))))

(defn handler
  [f s info]
    (s/connect
     (s/map f s)
    s))

(defn start-server
  [port f]
  (log/debug "Listening.. " port)
  (tcp/start-server
   (fn [s info]
     (handler f (wrap-duplex-stream protocol s) info))
   {:port port}))

(defn client
  ([node]
   (let [parts (str/split node #":")]
     (client (.get parts 0) (Integer. (.get parts 1)))))
  ([host port]
   (d/chain (tcp/client {:host host, :port port})
            #(wrap-duplex-stream protocol %))))

(defn send-forward-join
  ([to new-node from]
   (send-forward-join to new-node from 6))
  ([to new-node from arwl]
   (log/debug (str "Sending Forward-Join to: " to " new-node " new-node " from " from " arwl " arwl))
   (let [c @(client to)]
     (s/put! c {:type :forward-join :data {:new-node new-node :arwl arwl :sender from}}))))


(comment (def myc @(client "localhost" 10011)))
