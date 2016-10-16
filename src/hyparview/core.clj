(ns hyparview.core
  (:require
   [hyparview.transport :as transport]
   [hyparview.manager :as manager]
   [clojure.tools.logging :as log]
   [clojure.core.async :as async :refer [go-loop <! timeout]])
  (:gen-class))


(defn init-server
  []
  (transport/start-server 10013 manager/process-message))

(defn do-something
  []
  (while true
    (log/debug "Doing something.. ")
    (Thread/sleep 1000)))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!")
  (init-server)
  (do-something))
