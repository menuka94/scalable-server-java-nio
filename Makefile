# --------------------------------------------------------------------
# Author: Menuka Warushavithana
# --------------------------------------------------------------------

jarfile = ../../../libs/cs455-hw2-1.0-SNAPSHOT.jar

.PHONY: build
build:
	gradle build

run-server:
	gradle build
	cd build/classes/java/main && java -cp $(jarfile) cs455.scaling.server.Server

run-client:
	cd build/classes/java/main && java -cp $(jarfile) cs455.scaling.client.Client
