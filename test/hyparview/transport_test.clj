(ns hyparview.transport-test
  (:require [clojure.test :refer :all]
            [hyparview.transport :refer :all]
            [manifold.stream :as s]))

(deftest test-messages-dispath
  (testing "The messages dispatch is correct"
    (is (= (process-message [:join "join"]) "join"))
    (is (= (process-message [:disconnect "disconnect"]) "disconnect"))))

(defmacro with-server [server & body]
  `(let [server# ~server]
     (try
       ~@body
       (finally
         (.close ^java.io.Closeable server#)))))


(deftest test-server
  (with-server (start-server 10011)
    (let [c @(client "localhost" 10011)]
      (s/put! c [:join "test-join"])
      (is (= "test-join"  @(s/take! c))))))
