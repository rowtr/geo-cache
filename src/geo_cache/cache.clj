(ns geo-cache.cache
  (:import java.util.UUID)
  (:require [qbits.tardis :as uuid]))

(defprotocol IGeoCache
  (memoize-geocode  [this f])
  (memoize-weight   [this f]))


(defmulti get-cache :type)

(def ^:dynamic *cache* nil)

(defn coords->str
  [location]
  (str (:lat location) "," (:lng location)))  

(defn get-type1-uuid
  []
  (java.util.UUID/fromString (str (uuid/unique-time-uuid))))

(defn sha1 [s]
     (->> (-> "sha1"
              java.security.MessageDigest/getInstance
              (.digest (.getBytes s)))
          (map #(.substring
                 (Integer/toString
                  (+ (bit-and % 0xff) 0x100) 16) 1))
          (apply str)))

(defn shahash [from to]
  (sha1 (str (sha1 (coords->str from))(coords->str to))))



  
  
  
