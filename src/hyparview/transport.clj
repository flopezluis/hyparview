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

(defmulti process-message (fn [msg] (.get msg 0)))
(defmethod process-message :join
  [msg]
  (log/debug "join")
  (.get msg 1))

(defmethod process-message :disconnect
  [msg]
  (log/debug "disconnect")
  (.get msg 1))

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
  [s info]
    (s/connect
     (s/map process-message s)
    s))

(defn start-server
  [port]
  (log/debug "Listening.. " port)
  (tcp/start-server
   (fn [s info]
     (handler (wrap-duplex-stream protocol s) info))
   {:port port}))

(defn client
  [host port]
  (d/chain (tcp/client {:host host, :port port})
           #(wrap-duplex-stream protocol %)))

(comment (def myc @(client "localhost" 10011)))
