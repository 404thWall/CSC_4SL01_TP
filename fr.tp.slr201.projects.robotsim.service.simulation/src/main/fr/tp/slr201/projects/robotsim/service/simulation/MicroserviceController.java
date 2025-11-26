package main.fr.tp.slr201.projects.robotsim.service.simulation;

import com.fasterxml.jackson.core.JsonProcessingException;
import fr.tp.inf112.projects.robotsim.model.Factory;
import fr.tp.inf112.projects.robotsim.model.notifications.FactoryModelChangedNotifier;
import fr.tp.inf112.projects.robotsim.model.notifications.KafkaFactoryModelChangeNotifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

@SpringBootApplication
@RestController
public class MicroserviceController {

    private static final Logger LOGGER = Logger.getLogger(MicroserviceController.class.getName());
    private final HashMap<String, Factory> factories = new HashMap<>();
    private final InetAddress netAddress = InetAddress.getByName("localhost");
    private final int port = 8080;

    @Autowired
    private KafkaTemplate<String, Factory> simulationEventTemplate;

    public static void main(String[] args) {
        SpringApplication.run(MicroserviceController.class, args);
    }

    public MicroserviceController() throws UnknownHostException {
        LOGGER.info("MicroserviceController initialized with address " + netAddress + " and port " + port + ".");
    }

    @GetMapping("/start")
    public boolean startSimulation(@RequestParam(value = "factoryID", defaultValue = "defaultFactoryID") String factoryID) {
        LOGGER.info("Attempting to start simulation for factoryID " + factoryID + ".");
        if (factories.get(factoryID) == null) {
            try (Socket socket = new Socket(netAddress, port)) {
                OutputStream outStr = socket.getOutputStream();
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(outStr);
                objectOutputStream.writeObject(factoryID);

                InputStream inpStr = socket.getInputStream();
                ObjectInputStream objectInputStream = new ObjectInputStream(inpStr);

                Factory currentFactory = (Factory) objectInputStream.readObject();
                final FactoryModelChangedNotifier notifier = new KafkaFactoryModelChangeNotifier(currentFactory, simulationEventTemplate);
                currentFactory.setNotifier(notifier);
                LOGGER.info("Starting the simulation of factory " + factoryID + ".");

                currentFactory.startSimulation();
                factories.put(factoryID, currentFactory);
                LOGGER.info("Factory " + factoryID + " added to managed factories.");
                return true;

            } catch (IOException | ClassNotFoundException e) {
                LOGGER.log(Level.SEVERE, "Failed to start simulation for factoryID " + factoryID + ". Error : ", e);
                return false;
            }
        } else {
            factories.get(factoryID).startSimulation();
            return true;
        }

    }

    @GetMapping("/get")
    public Factory getFactory(@RequestParam(value = "factoryID", defaultValue = "defaultFactoryID") String factoryID) throws JsonProcessingException {
        LOGGER.info("Retrieving factory with ID " + factoryID + ".");
        LOGGER.info("Factory :" + factories.get(factoryID));

        return factories.get(factoryID);
    }

    @GetMapping("/stop")
    public void stopSimulation(@RequestParam(value = "factoryID", defaultValue = "defaultFactoryID") String factoryID) throws JsonProcessingException {
        LOGGER.info("Stopping simulation for factoryID " + factoryID + ".");
        Factory currentFactory = factories.get(factoryID);

        if (currentFactory != null) {
            currentFactory.stopSimulation();
            LOGGER.info("Simulation stopped for factoryID" + factoryID + ".");
        } else {
            LOGGER.warning("No factory found with ID " + factoryID + ".");
        }
    }
}
