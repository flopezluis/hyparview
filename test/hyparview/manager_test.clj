(ns hyparview.manager-test
  (:require [clojure.test :refer :all]
            [org.senatehouse.expect-call :refer :all]
            [hyparview.manager :refer :all]
            [hyparview.transport :refer :all]
            [manifold.stream :as s]))

(def member1 {:data {:new-node "127.0.0.1:5557"}})
(def member2 {:data {:new-node "127.0.0.1:5556"}})

(defn load-fixtures [] 
  (dosync
   (ref-set active-members []))
  (add-to-active-view member1)
  (add-to-active-view member2))

(deftest test-messages-dispath
  (testing "The messages dispatch is correct"
    (is (= (process-message {:type :join :data {:new-node "localhost:5558"}}) "localhost:5558"))
    (is (= (process-message {:type :disconnect :data {:new-node "localhost:5556"}} ) "localhost:5556"))))

(deftest test-is-there-a-slot-free?
  (testing "Tests if there are free slots in views"
    (load-fixtures)
    (is (= (is-there-a-slot-free? :active-view "127.0.0.1:5555") true))
    (is (= (is-there-a-slot-free? :active-view "127.0.0.1:5556" ) false))))

(deftest test-forward-join
  (testing "that the forward is sent to the active view"
    (load-fixtures)
    (expect-call [(send-forward-join ["127.0.0.1:5557" "localhost:5555" _])
                  (send-forward-join ["127.0.0.1:5556" "localhost:5555" _])]
                 ( forward-join-to-all-nodes  "localhost:5555"))))
