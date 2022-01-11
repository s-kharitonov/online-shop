FROM bellsoft/liberica-openjdk-alpine:17.0.1-12
RUN mkdir /app
COPY /build/libs/core-0.3.0.jar /app/core.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/core.jar"]