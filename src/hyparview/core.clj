(ns hyparview.core
  (:require
   [hyparview.transport :as transport]
   [hyparview.manager :as manager]
   [clojure.tools.logging :as log]
   [hyparview.config :refer [conf load-conf]]
   [clojure.tools.cli :refer [parse-opts]]
   [clojure.core.async :as async :refer [go-loop <! timeout]])
  (:gen-class))


(defn init-server
  []
  (transport/start-server manager/process-message))

(defn do-something
  []
  (while true
    (log/debug "Doing something.. ")
    (Thread/sleep 1000)))

(def cli-options
  ;; An option with a required argument
  [["-c" "--config CONFIG" "Confif file (edn)"
    :default "config.edn"]])

(defn start [config-file]
  (load-conf config-file)
  (init-server)
  (do-something))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (let [arguments (parse-opts args cli-options)]
    (start (:config (:options arguments)))))
