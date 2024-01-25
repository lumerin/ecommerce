package com.ecommerce.orderorchestration.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OrderOrchestrationService {

    final JmsTemplate jmsTemplate;

    @Autowired
    public OrderOrchestrationService(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    public void sendMessage(String queueName, Object object) {
        log.info("send message: {} data: {}", queueName, object);
        jmsTemplate.convertAndSend(queueName, object);
    }

}
