package fr.tp.inf112.projects.robotsim.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import fr.tp.inf112.projects.canvas.controller.Observer;
import fr.tp.inf112.projects.canvas.model.Canvas;
import fr.tp.inf112.projects.canvas.model.CanvasPersistenceManager;
import fr.tp.inf112.projects.canvas.model.impl.BasicVertex;
import fr.tp.inf112.projects.robotsim.app.SimulatorController;
import fr.tp.inf112.projects.robotsim.model.notifications.FactoryModelChangedNotifier;
import fr.tp.inf112.projects.robotsim.model.notifications.FactorySimulationEventConsumer;
import fr.tp.inf112.projects.robotsim.model.shapes.BasicVertexMixin;
import fr.tp.inf112.projects.robotsim.model.shapes.PositionedShape;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class RemoteSimulatorController extends SimulatorController {

    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final Logger LOGGER = Logger.getLogger(RemoteSimulatorController.class.getName());
    private static final String host = "localhost";
    private static final int port = 8081;
    private boolean running = false;

    public RemoteSimulatorController(CanvasPersistenceManager persistenceManager) {
        super(persistenceManager);
    }
    public RemoteSimulatorController(final Factory factoryModel,
                               final CanvasPersistenceManager persistenceManager) {
        super(factoryModel, persistenceManager);
    }

    private void updateViewer()
            throws InterruptedException, URISyntaxException, IOException {
        while (running) {
            final Factory remoteFactoryModel = getRemoteFactory();
            setCanvas(remoteFactoryModel);
            Thread.sleep(30);
        }
    }


//    public void setCanvasDeprecated(final Canvas canvasModel) {
//        LOGGER.info("Setting new canvas to "+ canvasModel + ".");
//        final List<Observer> observers = ((Factory) getCanvas()).getObservers();
//        super.setCanvas(canvasModel);
//        LOGGER.info("New canvas set.");
//        for (final Observer observer : observers) {
//            ((Factory) getCanvas()).addObserver(observer);
//        }
//        ((Factory) getCanvas()).notifyObservers();
//    }

    @Override
    public void setCanvas(final Canvas canvasModel) {
        LOGGER.info("Setting new canvas.");
        final FactoryModelChangedNotifier notifier = ((Factory) getCanvas()).getNotifier() ;
        super.setCanvas(canvasModel);
        LOGGER.info("New canvas set.");
        ((Factory) getCanvas()).setNotifier(notifier);
        ((Factory) getCanvas()).notifyObservers();
    }

    public void setCanvas(final String canvasModel) {
        LOGGER.info("Trying to convert to Factory the json string.");
        final PolymorphicTypeValidator typeValidator =
                BasicPolymorphicTypeValidator.builder()
                        .allowIfSubType(PositionedShape.class.getPackageName())
                        .allowIfSubType(Component.class.getPackageName())
                        .allowIfSubType(BasicVertex.class.getPackageName())
                        .allowIfSubType(ArrayList.class.getName())
                        .allowIfSubType(LinkedHashSet.class.getName())
                        .build();
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.activateDefaultTyping(typeValidator,
                        ObjectMapper.DefaultTyping.NON_FINAL)
                .addMixIn(BasicVertex.class, BasicVertexMixin.class);
        try {
            setCanvas(objectMapper.readValue(canvasModel, Factory.class));
        } catch (JsonProcessingException e) {
            LOGGER.log(Level.SEVERE, "An error occured while converting from Json to Factory");
            throw new RuntimeException(e);
        }
    }

    @Override
    public void startAnimation() {
        try {
            LOGGER.info("Attempting to start animation of "+getCanvas().getId());
            final URI uri = new URI("http", null, host, port, "/start", "factoryID="+getCanvas().getId(), null);
            HttpRequest request = HttpRequest.newBuilder().uri(uri).build();
            LOGGER.info("Request sent.");
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            LOGGER.info("Response from microservice "+response);
            running=true;
            //updateViewer();
            LOGGER.info("Attempting to start consuming messages");
            FactorySimulationEventConsumer consumer = new FactorySimulationEventConsumer(this);
            consumer.consumeMessages();
            LOGGER.info("Consumer launched");
        } catch (URISyntaxException | IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stopAnimation() {
        try {
            LOGGER.info("Attempting to stop animation of "+getCanvas().getId());
            final URI uri = new URI("http", null, host, port, "/stop", "factoryID="+getCanvas().getId(), null);
            HttpRequest request = HttpRequest.newBuilder().uri(uri).build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            LOGGER.info("Response from microservice "+response);
            running=false;
        } catch (URISyntaxException | IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public Factory getRemoteFactory() {
        try {
            LOGGER.info("Trying to fetch remote factory");
            final URI uri = new URI("http", null, host, port, "/get", "factoryID="+getCanvas().getId(), null);
            HttpResponse<String> response = httpClient.send(
                    HttpRequest.newBuilder().uri(uri).GET().build(),
                    HttpResponse.BodyHandlers.ofString()
            );
            LOGGER.info("Response from microservice "+response);
            //LOGGER.info("Response body :" + response.body() + ".");
            final ObjectMapper objectMapper = new ObjectMapper();
            PolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
                    .allowIfSubType(PositionedShape.class.getPackageName())
                    .allowIfSubType(Component.class.getPackageName())
                    .allowIfSubType(BasicVertex.class.getPackageName())
                    .allowIfSubType(ArrayList.class.getName())
                    .allowIfSubType(LinkedHashSet.class.getName())
                    .build();
            objectMapper.activateDefaultTyping(typeValidator,
                    ObjectMapper.DefaultTyping.NON_FINAL);

            objectMapper.addMixIn(BasicVertex.class, BasicVertexMixin.class);
            final Factory factory = objectMapper.readValue(response.body(), Factory.class);
            LOGGER.info("Managed to fetch factory.");
            return factory;
        } catch (URISyntaxException | IOException | InterruptedException e) {
            LOGGER.log(Level.SEVERE, "Failed to get remote factory :", e);
            return null;
        }
    }
    @Override
    public boolean isAnimationRunning() {
        return running;
    }
}
