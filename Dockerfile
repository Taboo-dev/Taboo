FROM openjdk:18
WORKDIR /taboo
COPY build/libs/Taboo-1.0-SNAPSHOT.jar taboo.jar
EXPOSE 8080

CMD ["java", "-jar", "./taboo.jar"]