package com.ecommerce.orderapi.service;


import com.ecommerce.orderapi.model.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class OrderService {
    @Autowired
    JmsTemplate jmsTemplate;

    public Mono<String> sendMessage(Order order) {
        try {
            jmsTemplate.convertAndSend("queue.orders", order);
            log.info("Order Created : {}", order);
            return Mono.just("Order Created");
        } catch (Exception e) {
            log.error("Error sendMessage try catch : {}", e.getMessage());
            return Mono.error(e.getCause());
        }
    }
}
