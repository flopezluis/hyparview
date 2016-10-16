(ns hyparview.transport-test
  (:require [clojure.test :refer :all]
            [hyparview.transport :refer :all]
            [hyparview.manager :refer :all]
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
  (with-server (start-server 10000 process-message)
    (let [c @(client "localhost" 10000)]
      (s/put! c {:type :join :data {:new-node "localhost:5555"}})
      (is (= "localhost:5555"  @(s/take! c))))))
