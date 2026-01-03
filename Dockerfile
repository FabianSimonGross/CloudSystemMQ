# BUILD STAGE
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn -q -e -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -q -DskipTests package spring-boot:repackage

# RUN STAGE
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy artifacts first (as root)
COPY --from=build /app/target/*.jar /app/app.jar
COPY templates /app/templates

# Create runtime dir and user, then fix permissions
RUN useradd -r -u 10001 appuser \
    && mkdir -p /app/running \
    && chown -R 10001:10001 /app

USER 10001

# MODE=master  -> gibt --master weiter
# MODE=node    -> gibt --node weiter
# MODE=        -> gibt nichts weiter
ENV MODE=""

ENTRYPOINT ["sh", "-lc", "\
  ARG=''; \
  if [ \"$MODE\" = 'master' ]; then ARG='--master'; fi; \
  if [ \"$MODE\" = 'node' ]; then ARG='--node'; fi; \
  exec java $JAVA_OPTS -jar /app/app.jar $ARG \
"]