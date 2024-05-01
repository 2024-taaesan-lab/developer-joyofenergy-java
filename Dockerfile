FROM amazoncorretto:17-alpine3.18-jdk
COPY build/libs/developer-joyofenergy-java.jar app.jar
CMD ["java", "-jar", "app.jar"]