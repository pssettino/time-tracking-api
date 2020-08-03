package com.scrumbox.mm.timetrackingapi.service;

import com.scrumbox.mm.timetrackingapi.commons.QueueConstants;
import com.scrumbox.mm.timetrackingapi.commons.QueueEvents;
import com.scrumbox.mm.timetrackingapi.commons.QueueMessage;
import com.scrumbox.mm.timetrackingapi.persistence.domain.Fichaje;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PublishService {

    private static final Logger log = LoggerFactory.getLogger(PublishService.class);


    private RabbitTemplate rabbitTemplate;

    @Autowired
    public PublishService(final RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void notifyFichajeCreated(Fichaje fichaje) {
        QueueMessage message = new QueueMessage();
        message.setEventName(QueueEvents.FICHAJE_CREATED);
        message.setData(fichaje.getDni());
        log.info("About to send message: {}" ,fichaje.getDni());
        rabbitTemplate.convertAndSend(QueueConstants.EXCHANGE_NAME, QueueConstants.ROUTING_KEY, message);
        log.info("Sent Message");

    }
}
