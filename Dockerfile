FROM eclipse-temurin:17-jdk AS build
WORKDIR /workspace

COPY .mvn .mvn
COPY mvnw mvnw
COPY pom.xml pom.xml
COPY src src
COPY META-INF META-INF

RUN chmod +x mvnw && ./mvnw -q clean package

FROM tomcat:9.0-jdk17-temurin
RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=build /workspace/target/mvc-hibernate-crud.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080
