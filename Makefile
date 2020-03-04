# --------------------------------------------------------------------
# Author: Menuka Warushavithana
# Server: <port-num> <thread-pool-size> <batch-size> <batch-time>
# Client: <server-host> <server-port> <message-rate>
# --------------------------------------------------------------------

jarfile = ../../../libs/cs455-hw2-1.0-SNAPSHOT.jar

.PHONY: build
build:
	gradle build

run-server:
	gradle build
	cd build/classes/java/main && java -cp $(jarfile) cs455.scaling.server.Server 5600 10 10 10

run-client:
	cd build/classes/java/main && java -cp $(jarfile) cs455.scaling.client.Client localhost 5600 4
