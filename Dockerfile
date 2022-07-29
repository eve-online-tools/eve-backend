# the first stage of our build will extract the layers
FROM eclipse-temurin:17.0.3_7-jre-jammy as builder
WORKDIR application
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} application.jar
RUN java -Djarmode=layertools -jar application.jar extract

# the second stage of our build will copy the extracted layers
FROM eclipse-temurin:17.0.3_7-jre-jammy
WORKDIR application
RUN addgroup --gid 1000 spring && useradd --uid 1000 --gid 1000 spring
USER spring:spring
COPY --from=builder application/dependencies/ ./
COPY --from=builder application/spring-boot-loader/ ./
COPY --from=builder application/snapshot-dependencies/ ./
COPY --from=builder application/application/ ./
ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]