FROM openjdk:8-jre


WORKDIR /
ADD ./omtd-annotation-viewer-server/target/omtd-annotation-viewer-server-0.0.1-SNAPSHOT.jar app.jar

CMD java -jar app.jar --viewer.omtd.store.url=http://83.212.101.85:8090
