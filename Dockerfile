FROM openjdk:8-jre-alpine
ENV APP_FILE streamer-0.0.1-fat.jar
ENV APP_HOME /usr/apps/

ARG JAR_FILE=build/libs/$APP_FILE
COPY $JAR_FILE $APP_HOME/
WORKDIR $APP_HOME

ENTRYPOINT ["sh", "-c"]
CMD ["exec java -Djava.security.egd=file:/dev/./urandom -jar $APP_FILE"]
EXPOSE 8080