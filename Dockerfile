FROM eclipse-temurin:17-jdk-jammy as build
WORKDIR /workspace/app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

RUN chmod +x mvnw
RUN ./mvnw install -DskipTests
RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)

RUN mkdir target/extracted
RUN java -Djarmode=layertools -jar target/*.jar extract --destination target/extracted

FROM eclipse-temurin:17-jdk-jammy
VOLUME /tmp

ARG EXTRACTED=/workspace/app/target/extracted
ARG AWS_ACCESS_KEY_ID
ARG AWS_SECRET_ACCESS_KEY
ARG AWS_SESSION_TOKEN

ENV AWS_ACCESS_KEY=${AWS_ACCESS_KEY_ID}
ENV AWS_SECRET_KEY=${AWS_SECRET_ACCESS_KEY}
ENV AWS_SESSION_TOKEN=${AWS_SESSION_TOKEN}

COPY --from=build ${EXTRACTED}/dependencies/ ./
COPY --from=build ${EXTRACTED}/spring-boot-loader/ ./
COPY --from=build ${EXTRACTED}/snapshot-dependencies/ ./
COPY --from=build ${EXTRACTED}/application/ ./

ENTRYPOINT ["java","org.springframework.boot.loader.launch.JarLauncher"]