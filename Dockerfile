FROM amazoncorretto:17-alpine-jdk
ADD config/keystore.p12 /etc/ssl/certs/keystore.p12
COPY target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]