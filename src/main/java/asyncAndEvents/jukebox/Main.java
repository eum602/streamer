package asyncAndEvents.jukebox;

import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static Logger logger = LoggerFactory.getLogger(Main.class);
    public static void main(String[] args) {
        logger.info("Starting the program");
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new Jukebox());
        vertx.deployVerticle(new NetControl());
    }
}
