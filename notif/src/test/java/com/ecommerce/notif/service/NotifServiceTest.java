package com.ecommerce.notif.service;

import com.ecommerce.notif.model.Notif;
import com.ecommerce.notif.model.OrderData;
import com.ecommerce.notif.model.ResponseNotif;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.jms.core.JmsTemplate;
import reactor.core.publisher.Mono;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotifServiceTest {

    @Mock
    private JmsTemplate jmsTemplate;

    @Mock
    private R2dbcEntityTemplate r2dbcEntityTemplate;

    @InjectMocks
    private NotifService notifService;

    @Test
    void sendNotif_Success() {
        OrderData orderData = new OrderData();
        orderData.setOrderId(1);
        orderData.setPriority(1);
        orderData.setData("Sample Data");

        when(r2dbcEntityTemplate.insert(any(Notif.class))).thenReturn(Mono.empty());

        notifService.sendNotif(orderData);

        verify(r2dbcEntityTemplate).insert(any(Notif.class));
        verify(jmsTemplate).convertAndSend(eq("queue.notif.status"), any(ResponseNotif.class));
    }

    @Test
    void sendNotif_Failure() {
        OrderData orderData = new OrderData();
        orderData.setOrderId(1);
        orderData.setPriority(1);
        orderData.setData("Data");

        when(r2dbcEntityTemplate.insert(any(Notif.class))).thenReturn(Mono.error(new RuntimeException()));

        notifService.sendNotif(orderData);

        verify(r2dbcEntityTemplate).insert(any(Notif.class));
        verify(jmsTemplate).convertAndSend(eq("queue.notif.status"), any(ResponseNotif.class));
    }
}