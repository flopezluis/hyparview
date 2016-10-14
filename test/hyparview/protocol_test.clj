(ns hyparview.protocol-test
  (:require [clojure.test :refer :all]
            [hyparview.protocol :refer :all]
            [manifold.stream :as s]))

(deftest test-messages-dispath
  (testing "The messages dispatch is correct"
    (is (= (process-message {:type :join :id "localhost:5555"}) "localhost:5555"))
    (is (= (process-message {:type :disconnect :id "localhost:5556"} ) "localhost:5556"))))
