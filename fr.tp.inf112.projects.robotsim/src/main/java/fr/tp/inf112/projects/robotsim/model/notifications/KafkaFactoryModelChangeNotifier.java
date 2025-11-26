package fr.tp.inf112.projects.robotsim.model.notifications;

import fr.tp.inf112.projects.canvas.controller.Observer;
import fr.tp.inf112.projects.robotsim.model.Factory;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.util.concurrent.CompletableFuture;

public class KafkaFactoryModelChangeNotifier implements FactoryModelChangedNotifier{

    private Factory factoryModel;
    private NewTopic topic;
    private KafkaTemplate<String, Factory> simulationEventTemplate;

    public KafkaFactoryModelChangeNotifier(Factory factoryModel, KafkaTemplate<String, Factory> simulationEventTemplate) {
        this.factoryModel=factoryModel;
        this.simulationEventTemplate = simulationEventTemplate;
        topic = TopicBuilder.name("simulation-" + factoryModel.getId()).build();
    }

    @Override
    public void notifyObserver() {
        final Message<Factory> factoryMessage = MessageBuilder.withPayload(factoryModel)
                .setHeader(KafkaHeaders.TOPIC, "simulation-" + factoryModel.getId())
                .build();
        final CompletableFuture<SendResult<String, Factory>> sendResult =
                simulationEventTemplate.send(factoryMessage);
        sendResult.whenComplete((result, ex) -> {
            if (ex != null) {
                throw new RuntimeException(ex);
            }
        });
    }

    @Override
    public boolean addObserver(Observer observer) {
        return false;
    }

    @Override
    public boolean removeObserver(Observer observer) {
        return false;
    }
}
