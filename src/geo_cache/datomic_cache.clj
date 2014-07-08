(ns geo-cache.datomic-cache
  (:require [geo-cache.cache :as cache :refer [get-cache shahash]])
  (:require [datomic.api :only (q db) :as d])
  (:import java.text.SimpleDateFormat)
  (:import java.util.Date)  )

(defn add-edge
  [data uri part]
  (try
	  (d/transact (-> uri d/connect) [(assoc data :db/id (d/tempid part))])
	  (ffirst (d/q '[:find ?id :in $ ?hash :where [?id :edge/id ?hash]](-> uri d/connect d/db) (:edge/id data)))
   (catch Exception e
	  (ffirst (d/q '[:find ?id :in $ ?hash :where [?id :edge/id ?hash]](-> uri d/connect d/db) (:edge/id data))))))

(defn add-address
  [data uri part]
  (try
    (d/transact (-> uri d/connect) [(assoc data :db/id (d/tempid part))])
    (ffirst (d/q '[:find ?addr :where [?addr :address/address _]](-> uri d/connect d/db) (:address/address data)))
   (catch Exception e
     (ffirst (d/q '[:find ?addr :where [?addr :address/address _]](-> uri d/connect d/db) (:address/address data))))))

(defn- get-weight-from-cache [this from to]
  (let [hash (shahash from to)
       q '[:find ?dist ?dur :in $ ?id :where [?e :edge/id ?id][?e :edge/distance ?dist][?e :edge/duration ?dur]]
       r (first (d/q q (-> (:uri this) d/connect d/db) hash))
       ret (if (nil? (first r)) nil (assoc {} :distance (first r) :duration (second r)))]
   ret))
(defn- add-weight-to-cache [this from to distance duration points]
  (let [hash (shahash from to)
        data (assoc {} :edge/id hash :edge/distance distance :edge/duration duration :edge/cache-date (Date.))
        e (add-edge data (:uri this) (:partition this))] (assoc {} :distance distance :duration duration)))
(defn- add-geocode-to-cache [this address lat lng]
  (let [data (assoc {} :address/address address :address/lat lat :address/lng lng :address/cache-date (Date.))
        e (add-address data (:uri this) (:partition this))]
    (assoc {} :address address :lat lat :lng lng)))
(defn- get-geocode-from-cache [this address]
  (let [q '[:find ?lat ?lng :in $ ?addr :where [?e :address/address ?addr][?e :address/lat ?lat][?e :address/lng ?lng]]
        r (first (d/q q (-> (:uri this) d/connect d/db) address))
        ret (if (nil? (first r)) nil (assoc {} :address address :lat (first r) :lng (second r)))]
    ret))
 
(defrecord DatomicCache []
  cache/IGeoCache  
  (memoize-geocode  [this f]
    (fn [addr]
      (if-let [e (get-geocode-from-cache this (:address addr))]
        (merge addr (select-keys e [:lat :lng]))
        (let [ret (f addr)]
          (add-geocode-to-cache this (:address ret) (:lat ret) (:lng ret))
          (merge addr ret)))))
  (memoize-weight   [this f]
    (fn [from to]
      (if-let [e (get-weight-from-cache this from to)]
        (select-keys e [:distance :duration :points])
        (let [ret (f from to)]
          (add-weight-to-cache this from to  (:distance ret) (:duration ret) (:points ret))
          ret)))))
       
(defmethod get-cache :datomic 
  [{:keys [uri partition] :as args }]
  (when (not (and uri partition)) (throw (Exception. (str "Missing required arguments uri: " uri " partition: " partition))))
  (DatomicCache. nil args))
    
