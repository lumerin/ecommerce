package com.ecommerce.orderorchestration.service;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.core.JmsTemplate;

@ExtendWith(MockitoExtension.class)
class OrderOrchestrationServiceTest {

    @Mock
    private JmsTemplate jmsTemplate;

    @InjectMocks
    private OrderOrchestrationService orderOrchestrationService;

    @Test
    void testSendMessage() {
        String queueName = "queue.test";
        Object messageObject = new Object();

        orderOrchestrationService.sendMessage(queueName, messageObject);

        Mockito.verify(jmsTemplate).convertAndSend(queueName, messageObject);
    }
}