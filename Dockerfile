FROM amazoncorretto:18-alpine-jdk AS build

WORKDIR /app

COPY gradle gradle
COPY build.gradle settings.gradle gradlew ./

RUN ./gradlew dependencies --refresh-dependencies

COPY src src

RUN ./gradlew build -x test

FROM amazoncorretto:18-alpine AS run

COPY --from=build /app/build/libs/api-0.0.1-SNAPSHOT.jar /app/app.jar

WORKDIR /app

ENV DATABASE_URL;
ENV DATABASE_USERNAME;
ENV DATABASE_PASSWORD;
ENV SEND_GRID_API_KEY;

ENTRYPOINT ["java", "-jar", "/app/app.jar"]