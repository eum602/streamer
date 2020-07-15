package asyncAndEvents.jukebox;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class EventBusHandlers extends AbstractVerticle {
    private static Logger logger = LoggerFactory.getLogger(EventBusHandlers.class);
    private Vertx vertx;

    public EventBusHandlers(Vertx vertx) {
        this.vertx = vertx;
    }

    public void list(Message<?> request) {
        vertx.fileSystem().readDir("tracks", ".*mp3$", ar -> {
            if (ar.succeeded()) {
                List<String> files = ar.result()
                        .stream()
                        .map(File::new)
                        .map(File::getName)
                        .collect(Collectors.toList());
                JsonObject json = new JsonObject().put("files", new JsonArray(files));
                request.reply(json);
            } else {
                logger.error("readDir failed", ar.cause());
                request.fail(500, ar.cause().getMessage());
            }
        });
    }

    public void play(Message<?> request, Jukebox jukebox) {
        logger.info("Play");
        jukebox.setCurrentMode(Shared.State.PLAYING);
    }

    public void pause(Message<?> request, Jukebox jukebox) {
        logger.info("Pause");
        jukebox.setCurrentMode(Shared.State.PAUSED);
    }

    public void schedule(Message<JsonObject> request, Jukebox jukebox) {
        String file = request.body().getString("file");
        logger.info("Scheduling {}", file);
        if (jukebox.getPlaylist().isEmpty() && jukebox.getCurrentMode() == Shared.State.PAUSED) {
            logger.info("Setting to PLAYING");
            jukebox.setCurrentMode(Shared.State.PLAYING);
        }
        jukebox.getPlaylist().offer(file);
    }
}