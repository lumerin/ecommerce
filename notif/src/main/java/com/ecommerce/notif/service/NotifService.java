package com.ecommerce.notif.service;


import com.ecommerce.notif.model.Notif;
import com.ecommerce.notif.model.OrderData;
import com.ecommerce.notif.model.ResponseNotif;
import com.ecommerce.notif.model.enums.NotifStatus;
import com.ecommerce.notif.repository.NotifRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class NotifService {

    @Autowired
    NotifRepository notifRepository;
    @Autowired
    JmsTemplate jmsTemplate;
    @Autowired
    R2dbcEntityTemplate r2dbcEntityTemplate;

    public void  sendNotif(OrderData data) {
        Notif notifData = new Notif();
        notifData.setOrderId(data.getOrderId());
        notifData.setStatus("NOTIF_COMPLETED");
        notifData.setCreatedDate(LocalDate.now());
        log.info("notif datanya :{}", notifData);
        r2dbcEntityTemplate.insert(notifData)
                .doOnSuccess(registered -> {
                    ResponseNotif responseNotif = new ResponseNotif();
                    responseNotif.setOrderId(data.getOrderId());
                    responseNotif.setPriority(data.getPriority());
                    responseNotif.setData(data.getData());
                    responseNotif.setNotifStatus("NOTIF_COMPLETED");
                    jmsTemplate.convertAndSend("queue.notif.status", responseNotif);
                    log.info("Message sent to orchestrator that notif successfully registered: {}", responseNotif);
                })
                .doOnError(error -> {
                    log.info("notif failed because : {}", error.getMessage());
                    jmsTemplate.convertAndSend("queue.notif.status", "NOTIF_REJECTED");
                    log.info("message sent to orchestrator that notif rejected: {}", NotifStatus.NOTIF_REJECTED);
                }).subscribe();
    }

    public void rollbackRegister(String notifData) {
        notifRepository.deleteById(notifData).subscribe();
    }
}
