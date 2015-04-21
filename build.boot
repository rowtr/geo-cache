(set-env!
  :dependencies  '[[adzerk/bootlaces            "0.1.11" :scope "test"]
                   [com.novemberain/monger      "2.0.0"]
                   [com.taoensso/faraday        "1.6.0"]
                   [com.taoensso/carmine        "2.9.2"]
                   [com.datomic/datomic-free    "0.9.5153"]
                   
                   
                   ]
  :resource-paths #{"src"})

(def +version+ "0.0.7")

(task-options!
 pom  {:project     'raywillig/geo-cache
       :version     +version+
       :description "various cache mechanisms for geocodes and directions"
       :url         "https://github.com/cljsjs/packages"
       :scm         {:url "https://github.com/cljsjs/packages"}
       :license     {"" ""}})
