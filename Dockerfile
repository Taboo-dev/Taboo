FROM openjdk:18
WORKDIR /taboo
COPY build/libs/Taboo-1.0-SNAPSHOT.jar taboo.jar
COPY application.yml application.yml
EXPOSE 8080

CMD ["java", "-jar", "./taboo.jar"]