package asyncAndEvents.jukebox;

import io.vertx.core.Vertx;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class MusicHttpHandler {
    private static Logger logger = LoggerFactory.getLogger(MusicHttpHandler.class);
    private Vertx vertx;

    public MusicHttpHandler(Vertx vertx) {
        this.vertx = vertx;
    }

    public void httpHandler(HttpServerRequest request, Set<HttpServerResponse> streamers) {
        logger.info("{} '{}' {}", request.method(), request.path(), request.remoteAddress());
        if ("/".equals(request.path())) {
            openAudioStream(streamers,request);
            return;
        }

        if (request.path().startsWith("/download/")) {
            String sanitizedPath = request.path().substring(10).replaceAll("/", "");
            download(sanitizedPath, request);
            return;
        }

        request.response().setStatusCode(404).end();
    }

    //private final Set<HttpServerResponse> streamers = new HashSet<>();

    private void openAudioStream(Set<HttpServerResponse> streamers, HttpServerRequest request) {
        logger.info("New streamer");
        HttpServerResponse response = request.response()
                .putHeader("Content-Type", "audio/mpeg")
                .setChunked(true);
        streamers.add(response);
        response.endHandler(v -> {
            streamers.remove(response);
            logger.info("A streamer left");
        });
    }
    public void download(String path, HttpServerRequest request) {
        String file = "tracks/" + path;
        try{
            if (!vertx.fileSystem().existsBlocking(file)) {
                request.response().setStatusCode(404).end();
                return;
            }
        }catch (Exception e){
            logger.error("Error: " + e);
        }

        OpenOptions opts = new OpenOptions().setRead(true);
        vertx.fileSystem().open(file, opts, ar -> {
            if (ar.succeeded()) {
                downloadFilePipe(ar.result(), request);
            } else {
                logger.error("Read failed", ar.cause());
                request.response().setStatusCode(500).end();
            }
        });
    }

    private void downloadFile(AsyncFile file, HttpServerRequest request) {
        HttpServerResponse response = request.response();
        response.setStatusCode(200)
                .putHeader("Content-Type", "audio/mpeg")
                .setChunked(true);

        file.handler(buffer -> {
            response.write(buffer);
            if (response.writeQueueFull()) {
                file.pause();
                response.drainHandler(v -> file.resume());
            }
        });

        file.endHandler(v -> response.end());
    }

    private void downloadFilePipe(AsyncFile file, HttpServerRequest request) {
        HttpServerResponse response = request.response();
        response.setStatusCode(200)
                .putHeader("Content-Type", "audio/mpeg")
                .setChunked(true);
        file.pipeTo(response);
    }

}
