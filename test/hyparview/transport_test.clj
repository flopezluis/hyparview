(ns hyparview.transport-test
  (:require [clojure.test :refer :all]
            [hyparview.transport :refer :all]
            [hyparview.manager :refer :all]
            [hyparview.config :refer [conf load-conf]]
            [manifold.stream :as s]))

(defmacro with-server [server & body]
  `(let [server# ~server]
     (try
       ~@body
       (finally
         (.close ^java.io.Closeable server#)))))


(deftest test-server
  (dosync
   (ref-set active-members []))
  (with-redefs [conf {:address "127.0.0.1" :port 10000 :max-active-view 5}] 
                (with-server (start-server process-message)
                  (let [c @(client "127.0.0.1" 10000)]
                    (s/put! c {:type :join :data {:new-node "localhost:5555"}})
                    (is (= "localhost:5555"  @(s/take! c)))))))
