package org.ulpgc.dacd.control;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.ulpgc.dacd.control.exceptions.EventReceiverException;

import javax.jms.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TopicSubscriber implements Suscriber {
    private static final Logger logger = Logger.getLogger(TopicSubscriber.class.getName());
    private Connection connection;
    private Session session;
    private final EventStoreBuilder fileEventStoreBuilder;
    private static final String brokerUrl = "tcp://localhost:61616";
    private static final String topicName = "prediction.Weather";
    private static final String clientID = "EventStoreBuilder";

    public TopicSubscriber(FileEventStoreBuilder fileEventStoreBuilder) {
        this.fileEventStoreBuilder = fileEventStoreBuilder;
    }

    @Override
    public void start() throws EventReceiverException {
        try {
            initializeConnection();
            initializeSession();
            initializeMessageListener();
        } catch (JMSException e) {
            throw new EventReceiverException("Error creating listener", e);
        }
    }

    private void initializeConnection() throws JMSException {
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
        connection = connectionFactory.createConnection();
        connection.setClientID(clientID);
        connection.start();
    }

    private void initializeSession() throws JMSException {
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    }

    private void initializeMessageListener() throws JMSException {
        Topic topic = session.createTopic(topicName);
        MessageConsumer consumer = session.createDurableSubscriber(topic, clientID + topicName);
        consumer.setMessageListener(this::processMessage);
    }

    private void processMessage(Message message) {
        if (message instanceof TextMessage) {
            try {
                String receivedMessage = ((TextMessage) message).getText();
                fileEventStoreBuilder.save(receivedMessage);
                logger.info("Received message: " + receivedMessage);
            } catch (JMSException e) {
                logger.log(Level.SEVERE, "Error processing message", e);
            }
        }
    }
}
