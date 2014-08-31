(ns example-clojure-rest.handler
      (:import com.jolbox.bonecp.BoneCPDataSource)
      (:use compojure.core)
      (:use cheshire.core)
      ;(:use ring.util.response)
      (:require [compojure.handler :as handler]
                [ring.util.request :as req-util]
                [ring.util.response :as res-util]
                [ring.middleware.json :as middleware]
                [clojure.java.jdbc :as jdbc]
                [clojurewerkz.scrypt.core :as password]
                [java-jdbc.sql :as sql]
                [compojure.route :as route]))

    (def db-spec 
  {:classname "com.mysql.jdbc.Driver"
   :subprotocol "mysql"
   :subname "//127.0.0.1:3306/c9"
   :user "svozza"
   :password ""})

    (defn pool
  [spec]
  (let [partitions 3
        cpds (doto (BoneCPDataSource.)
               (.setJdbcUrl (str "jdbc:" (:subprotocol spec) ":" (:subname spec)))
               (.setUsername (:user spec))
               (.setPassword (:password spec))
               (.setPartitionCount partitions)
               (.setStatisticsEnabled true)
               (.setIdleConnectionTestPeriodInMinutes 25)
               (.setIdleMaxAgeInMinutes (* 3 60))
               (.setConnectionTestStatement "/* ping *\\/ SELECT 1"))] 
    {:datasource cpds}))

    (def pooled-db (pool db-spec))

    (defn db-connection [] @pooled-db)
    
    (defn login [user]
      (let [results (jdbc/query pooled-db (sql/select "password" :USERS (sql/where {:login (user "login")})))]
          (cond
            (or (empty? results) (not (contains? user "password"))) {:status 401}
            (password/verify (user "password") ((first results) :password)) {:status 200}
            :else {:status 401})))
    
    (defn default-get [table]
      (res-util/response
          (jdbc/query pooled-db
            (sql/select * table))))
    
    (defn default-get-with-id [table id]
      (let [results (jdbc/query pooled-db (sql/select * table (sql/where {:id id})))]
          (cond
            (empty? results) {:status 404}
            :else (res-util/response (first results)))))
          
    (defn default-post [table request body]
      (jdbc/with-db-transaction [trans-conn pooled-db]
        (jdbc/insert! trans-conn table body)
         (let [results (jdbc/query trans-conn ["SELECT LAST_INSERT_ID()"])
               url (str (req-util/request-url request) "/")
               location (str url (first (vals (first results))))]
              {:status 201, :headers {"Location" location}})))
            
    (defn add-user [request user]
        (let [encrypted-pwd (password/encrypt (user "password") 16384 8 1)
              user-with-encrypted-pwd (assoc user "password" encrypted-pwd)]
            (default-post :USERS request user-with-encrypted-pwd)))
  
    (defn default-update [table user id]
        (jdbc/with-db-transaction [trans-conn pooled-db]
          (jdbc/update! trans-conn table user (sql/where {:id id}))
          (jdbc/query trans-conn (sql/select * table (sql/where {:id id})))))
        
    (defn update-user [id user]
      (if (contains? user "password")
          (let [user-with-encrypted-pwd (assoc user "password" (password/encrypt (user "password") 16384 8 1)) ]
               (default-update :USERS user-with-encrypted-pwd id))
          (default-update :USERS user id)))
    
    (defn default-delete [table id]
      (jdbc/delete! pooled-db table (sql/where {:id id}))
      {:status 204})
      
    (defroutes app-routes
      (GET "/" [] (res-util/resource-response "index.html" {:root "public"}))
      (route/resources "/")
      (POST "/login" {body :body} (login body))
      
      (context "/api" [] (defroutes api
        (context "/users" [] (defroutes users-routes
          (GET  "/" [] (default-get :USERS))
          (POST "/" {:as request body :body} (add-user request body))
          (context "/:id" [id] (defroutes user-routes
            (GET    "/" [] (default-get-with-id :USERS id))
            (PUT    "/" {body :body} (update-user id body))
            (DELETE "/" [] (default-delete :USERS id))))))
          
        (context "/images" [] (defroutes users-routes
          (GET  "/" [] (default-get :IMAGES))
          (POST "/" {:as request body :body} (default-post :IMAGES request body))
          (context "/:id" [id] (defroutes user-routes
            (GET    "/" [] (default-get-with-id :IMAGES id))
            (PUT    "/" {body :body} (default-update :IMAGES body id))
            (DELETE "/" [] (default-delete :IMAGES id))))))
          
      (route/not-found "Not Found"))))

    (def app
        (-> (handler/api app-routes)
            (middleware/wrap-json-body)
            (middleware/wrap-json-response)))