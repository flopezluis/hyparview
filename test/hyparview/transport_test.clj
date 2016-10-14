(ns hyparview.transport-test
  (:require [clojure.test :refer :all]
            [hyparview.transport :refer :all]
            [hyparview.protocol :refer :all]
            [manifold.stream :as s]))

(defmacro with-server [server & body]
  `(let [server# ~server]
     (try
       ~@body
       (finally
         (.close ^java.io.Closeable server#)))))


(deftest test-server
  (with-server (start-server 10000 process-message)
    (let [c @(client "localhost" 10000)]
      (s/put! c {:type :join :id "localhost:5555"})
      (is (= "localhost:5555"  @(s/take! c))))))
