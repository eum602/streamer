package asyncAndEvents.jukebox;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

public class Jukebox extends AbstractVerticle {
    private EventBusHandlers eventBusHandlers;
    private MusicHttpHandler musicHttpHandler;
    private AudioChunker audioChunker;

    private final Set<HttpServerResponse> streamers = new HashSet<>();
    private Shared.State currentMode = Shared.State.PAUSED;
    public final Queue<String> playlist = new ArrayDeque<>();

    @Override
    public void start() throws Exception {
        EventBus eventBus = vertx.eventBus();
        musicHttpHandler= new MusicHttpHandler(vertx);
        audioChunker = new AudioChunker(vertx);
        eventBusHandlers = new EventBusHandlers(vertx);
        eventBus.consumer("jukebox.list", eventBusHandlers::list);
        eventBus.consumer("jukebox.schedule", e -> eventBusHandlers.schedule((Message) e, this));
        eventBus.consumer("jukebox.play", e -> eventBusHandlers.play(e,this));
        eventBus.consumer("jukebox.pause", e-> eventBusHandlers.pause(e,this));


        vertx.createHttpServer()
                .requestHandler(this::httpHandler)
                .listen(8080);
        vertx.setPeriodic(100,this::streamAudioChunk); //Data streamed(pushed from read to write) every 100ms.
    }

    private void httpHandler(HttpServerRequest request) {
        musicHttpHandler.httpHandler(request,streamers);
    }

    private void streamAudioChunk(long id){
        audioChunker.streamAudioChunk(id,this);
    }

    public void setCurrentMode(Shared.State currentMode) {
        this.currentMode = currentMode;
    }

    public Queue<String> getPlaylist() {
        return playlist;
    }

    public Shared.State getCurrentMode() {
        return currentMode;
    }

    public Set<HttpServerResponse> getStreamers() {
        return streamers;
    }
}