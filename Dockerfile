FROM amazoncorretto:17-alpine-jdk AS build

WORKDIR /app

COPY gradle gradle
COPY build.gradle settings.gradle gradlew ./

RUN ./gradlew dependencies --refresh-dependencies

COPY src src

RUN ./gradlew build -x test

FROM amazoncorretto:17-alpine AS run

WORKDIR /app

COPY --from=build /app/build/libs/subfootball.tracker-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]