## Streamer

To build the "fat jar"
```shell script
JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64 && ./gradlew clean shadowJar
./gradlew clean
./gradlew shadowJar
```

Because the application plugin is being used, you may directly run the application:

```shell script
$     ./gradlew run
```

You may also run the fat jar as a standalone runnable jar:
```shell script
$ java -jar build/libs/streamer-0.0.1-fat.jar
```

## Build with docker
```shell script
docker build -t eumb602/streamer:0.0.1 .
``` 
## Run with docker-compose:
```shell script
$ docker-compose up --build -d
```
### Downloading files
curl -o out.mp3 http://localhost:8080/download/YuriBuenaventura-ValledeRosas.mp3
