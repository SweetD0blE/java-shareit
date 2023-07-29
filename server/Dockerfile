FROM amazoncorretto:11
#ENV TZ=Europe/Moscow
COPY target/*.jar server.jar
ENTRYPOINT ["java","-jar","/server.jar"]