FROM openjdk:11-slim

COPY target/microservice-bank-accounts-0.0.1-SNAPSHOT.jar microservice-bank-accounts-0.0.1-SNAPSHOT.jar

ENTRYPOINT ["java","-jar","microservice-bank-accounts-0.0.1-SNAPSHOT.jar"]

