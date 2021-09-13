FROM bellsoft/liberica-openjdk-alpine:16.0.2-7
RUN mkdir /app
COPY /build/libs/core-0.1.0.jar /app/core.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/core.jar"]