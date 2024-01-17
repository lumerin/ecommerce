package com.ecommerce.orderorchestration.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
@Slf4j
public class OrderOrchestrationService {

    final JmsTemplate jmsTemplate;
    final R2dbcEntityTemplate r2dbcEntityTemplate;

    @Autowired
    public OrderOrchestrationService(JmsTemplate jmsTemplate, R2dbcEntityTemplate r2dbcEntityTemplate) {
        this.jmsTemplate = jmsTemplate;
        this.r2dbcEntityTemplate = r2dbcEntityTemplate;
    }

    public void sendMessage(String queueName, Object object) {
        log.info("send message: {} data: {}", queueName, object);
        jmsTemplate.convertAndSend(queueName, object);
    }

}
