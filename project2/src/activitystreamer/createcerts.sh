keytool -export -file server.cert -keystore server.jks -storepass admin123 -alias server
keytool -export -file client.cert -keystore client.jks -storepass admin123 -alias client

keytool -import -file client.cert -keystore server.jks -storepass admin123 -alias client
keytool -import -file server.cert -keystore client.jks -storepass admin123 -alias server
