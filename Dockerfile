FROM openjdk:8 as Target
COPY build/libs/time-tracking-api-0.0.1.jar time-tracking-api.jar

ENTRYPOINT ["java","-jar","/time-tracking-api.jar"]

EXPOSE 9093
