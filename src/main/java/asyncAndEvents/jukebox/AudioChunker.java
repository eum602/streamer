package asyncAndEvents.jukebox;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.http.HttpServerResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;
import java.util.Set;

public class AudioChunker {
    private final Logger logger = LoggerFactory.getLogger(AudioChunker.class);

    private AsyncFile currentFile;
    private long positionInFile;

    private Vertx vertx;

    public AudioChunker(Vertx vertx) {
        this.vertx = vertx;
    }

    public void streamAudioChunk(long id, Jukebox jukebox) {
        if (jukebox.getCurrentMode() == Shared.State.PAUSED) {
            return;
        }
        if (currentFile == null && jukebox.getPlaylist().isEmpty()) {
            jukebox.setCurrentMode(Shared.State.PAUSED);
            return;
        }
        if (currentFile == null) {
            openNextFile(jukebox.getPlaylist());
        }
        currentFile.read(Buffer.buffer(4096), 0, positionInFile, 4096, ar -> {
            if (ar.succeeded()) {
                processReadBuffer(ar.result(),jukebox.getStreamers());
            } else {
                logger.error("Read failed", ar.cause());
                closeCurrentFile();
            }
        });
    }

    private void processReadBuffer(Buffer buffer, Set<HttpServerResponse> streamers) {
        logger.info("Read {} bytes from pos {}", buffer.length(), positionInFile);
        positionInFile += buffer.length();
        if (buffer.length() == 0) {
            closeCurrentFile();
            return;
        }
        for (HttpServerResponse streamer : streamers) {
            if (!streamer.writeQueueFull()) {//back pressure technique
                streamer.write(buffer.copy());
            }
        }
    }

    private void openNextFile(Queue<String> playlist) {
        OpenOptions opts = new OpenOptions().setRead(true);
        currentFile = vertx.fileSystem()
                .openBlocking("tracks/" + playlist.poll(), opts);
        positionInFile = 0;
    }

    private void closeCurrentFile() {
        positionInFile = 0;
        currentFile.close();
        currentFile = null;
    }
}
