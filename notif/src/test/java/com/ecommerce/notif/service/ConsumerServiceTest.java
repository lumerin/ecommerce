package com.ecommerce.notif.service;

import com.ecommerce.notif.model.OrderData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.Message;

import static org.mockito.Mockito.*;

public class ConsumerServiceTest {

    @Mock
    private NotifService notifService;

    @Mock
    private Message<OrderData> message;

    @InjectMocks
    private ConsumerService consumerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testReceiveSendNotif() {
        OrderData orderData = new OrderData();
        when(message.getPayload()).thenReturn(orderData);

        consumerService.receiveSendNotif(message);

        verify(notifService).sendNotif(orderData);
    }

    @Test
    void testReceiveSendNotifWithError() {
        when(message.getPayload()).thenThrow(new RuntimeException());

        consumerService.receiveSendNotif(message);

        verifyNoInteractions(notifService);
    }
}