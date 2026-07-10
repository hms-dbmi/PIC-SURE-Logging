# amazoncorretto:25 matches pic-sure-gateway and pic-sure-operations-service.
FROM amazoncorretto:25
WORKDIR /app
# spring-boot-maven-plugin:repackage also emits *.jar.original, which this glob excludes.
COPY target/pic-sure-logging-*.jar app.jar
RUN mkdir -p /app/logs
# logging.env sets PORT=80; the gateway reaches this service on port 80.
EXPOSE 80
# No HEALTHCHECK: the base image has no wget/curl, and gateway/operations-service
# carry none either. Health is polled externally via GET /health.
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /app/app.jar"]
