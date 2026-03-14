FROM eclipse-temurin:25-jdk

WORKDIR /app

COPY pom.xml .
RUN apt-get update && apt-get install -y maven
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B -DskipTests clean package

CMD ["java", "-jar", "target/discordBot-1.0-SNAPSHOT-jar-with-dependencies.jar"]