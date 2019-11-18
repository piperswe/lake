FROM clojure AS builder

COPY . /usr/src/app
WORKDIR /usr/src/app
RUN lein uberjar

FROM openjdk

COPY --from=builder /usr/src/app/target/lake-*-standalone.jar /lake.jar
CMD java -jar /lake.jar
