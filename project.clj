(defproject org.clojars.raywillig/geo-cache "0.0.1"
  :description "library for caching geo data"  
  :url "https://github.com/rwillig/geo-cache"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :scm     {:name "git "
            :url  "https://github.com/rwillig/geo-cache"}          
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [cc.qbits/tardis "0.3.1"]
                 [com.novemberain/monger "1.5.0"]
                 [com.datomic/datomic-free "0.8.3664"]])
