package com.ecommerce.notif.service;


import com.ecommerce.notif.model.Notif;
import com.ecommerce.notif.model.OrderData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;

@Service
@Slf4j
public class ConsumerService {

    @Autowired
    NotifService notifService;

    @JmsListener(destination = "queue.notif")
    public void receiveSendNotif(Message<OrderData> message) {
        try {
            notifService.sendNotif(message.getPayload());
        } catch (Exception e) {
            log.error("Error while send notif : {}", e.getMessage());
        }
    }

    @JmsListener(destination = "queue.rollback.notif")
    public void rollbackRegister(Message<String> message) {
        try {
            notifService.rollbackRegister(message.getPayload());
        } catch (Exception e) {
            log.error("Error while rollback notif : {}", e.getMessage());
        }
    }
}
