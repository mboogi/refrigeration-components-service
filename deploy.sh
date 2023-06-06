./#!/bin/sh
./gradlew build -x test  #-Dspring.profiles.active=dev
 #&& java -jar build/libs/components-selector-0.0.1-SNAPSHOT.jar
sudo docker build --build-arg JAR_FILE=build/libs/components-selector-0.0.1-SNAPSHOT.jar -t bogmilos/be-components-selector:2.0.8 .
#sudo docker run -p 8080:8080 bogmilos/be-sniffer-service:latest
sudo docker login
sudo docker push bogmilos/be-components-selector:2.0.8
