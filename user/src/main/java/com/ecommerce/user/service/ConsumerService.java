package com.ecommerce.user.service;

import com.ecommerce.user.model.OrderData;
import com.ecommerce.user.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ConsumerService {

    @Autowired
    UserService userService;

    @Autowired
    JmsTemplate jmsTemplate;

    @JmsListener(destination = "queue.user.register")
    public void receiveRegister(Message<OrderData> message) {
        try {
            log.info("whole data : {}", message.getPayload().getData());
            userService.register(message.getPayload());
        } catch (Exception e) {
            log.error("Error while register : {}", e.getMessage());
            jmsTemplate.convertAndSend("queue.user.register.status", "USER_REJECTED");
        }
    }

    @JmsListener(destination = "queue.user.register.rollback")
    public void rollbackRegister(Message<OrderData> message) {
        try {
            log.info("whole data : {}", message.getPayload().getData());
            userService.rollbackRegister(message.getPayload());
        } catch (Exception e) {
            log.error("Error while rollback register : {}", e.getMessage());
        }
    }
}
