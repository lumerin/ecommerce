package com.ecommerce.user.service;

import com.ecommerce.user.model.OrderData;
import com.fasterxml.jackson.core.JacksonException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.Message;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ConsumerServiceTest {

    @InjectMocks
    private ConsumerService consumerService;

    @Mock
    private UserService userService;

    @Mock
    private JmsTemplate jmsTemplate;

    @Mock
    private Message<OrderData> message;

    @Test
    void testReceiveRegister() {
        OrderData orderData = new OrderData();
        when(message.getPayload()).thenReturn(orderData);

        consumerService.receiveRegister(message);

        verify(userService).register(orderData);
    }

    @Test
    void testReceiveRegisterException() {
        OrderData orderData = new OrderData();
        when(message.getPayload()).thenReturn(orderData);

        doThrow(new RuntimeException()).when(userService).register(any(OrderData.class));

        consumerService.receiveRegister(message);

        verify(jmsTemplate).convertAndSend("queue.user.register.status", "USER_REJECTED");
        verify(userService).register(any(OrderData.class));
    }

    @Test
    void testRollbackRegister() {
        OrderData orderData = new OrderData();
        when(message.getPayload()).thenReturn(orderData);

        consumerService.rollbackRegister(message);

        verify(userService).rollbackRegister(orderData);
    }

    @Test
    void testRollbackRegisterException() {
        when(message.getPayload()).thenThrow(new RuntimeException());

        consumerService.rollbackRegister(message);

        verifyNoInteractions(userService);
        verifyNoInteractions(jmsTemplate);
    }
}