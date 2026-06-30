FROM amazoncorretto:25-alpine

WORKDIR /app

COPY target/port-view-0.0.1-SNAPSHOT.jar /app/port-view.jar

EXPOSE 8080

ENV JAVA_OPTS=""

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/port-view.jar"]