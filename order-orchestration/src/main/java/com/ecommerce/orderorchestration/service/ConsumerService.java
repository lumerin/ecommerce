package com.ecommerce.orderorchestration.service;

import com.ecommerce.orderorchestration.model.Order;
import com.ecommerce.orderorchestration.model.OrderData;
import com.ecommerce.orderorchestration.model.ResponseNotif;
import com.ecommerce.orderorchestration.model.ResponseUser;
import com.ecommerce.orderorchestration.repository.OrderActionRepository;
import com.ecommerce.orderorchestration.repository.OrderStepRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class ConsumerService {

    final OrderOrchestrationService orderOrchestrationService;
    final OrderActionRepository actionRepository;
    final OrderStepRepository stepRepository;

    @Autowired
    public ConsumerService(OrderOrchestrationService orderOrchestrationService, OrderStepRepository stepRepository, OrderActionRepository actionRepository, R2dbcEntityTemplate r2dbcEntityTemplate) {
        this.orderOrchestrationService = orderOrchestrationService;
        this.actionRepository = actionRepository;
        this.stepRepository = stepRepository;
    }


    @JmsListener(destination = "queue.orders")
    public void processOrder(Message<Order> message) {
        log.info("data masuk  : {}", message.getPayload().getAction());
        actionRepository.findByAction(message.getPayload().getAction().toUpperCase())
            .doOnSuccess(found -> {
                log.info("data action : {}", found.getAction());
                stepRepository.findByOrderIdAndPriority(found.getId(), 0)
                    .doOnSuccess(stepFound -> {
                        OrderData orderData = new OrderData();
                        orderData.setOrderId(found.getId());
                        orderData.setPriority(0);
                        orderData.setData(message.getPayload().getData());
                        log.info("Data before send message : {}", orderData.getData());
                        orderOrchestrationService.sendMessage(stepFound.getStep(), orderData);
                    }).subscribe();
            }).subscribe();
    }

    @JmsListener(destination = "queue.user.register.status")
    public void statusRegister(Message<ResponseUser> message) {
        if (message.getPayload().getUserStatus().equals("USER_APPROVED")) {
            stepRepository.findByOrderIdAndPriority(message.getPayload().getOrderId(), message.getPayload().getPriority()+1)
                .doOnSuccess(stepFound -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("id", message.getPayload().getId());
                    data.put("name", message.getPayload().getName());
                    data.put("username", message.getPayload().getUsername());
                    data.put("password", message.getPayload().getPassword());
                    data.put("balance", message.getPayload().getBalance());
                    data.put("status", message.getPayload().isStatus());

                    OrderData orderData = new OrderData();
                    orderData.setOrderId(message.getPayload().getOrderId());
                    orderData.setPriority(message.getPayload().getPriority()+1);
                    orderData.setData(data);
                    log.info("Data before send message : {}", orderData.getData());
                    orderOrchestrationService.sendMessage(stepFound.getStep(), orderData);
                }).subscribe();
        }
    }

    @JmsListener(destination = "queue.notif.status")
    public void statusNotif(Message<ResponseNotif> message) {
        if (message.getPayload().getNotifStatus().equals("NOTIF_COMPLETED")) {
            stepRepository.findByOrderIdAndPriority(message.getPayload().getOrderId(), message.getPayload().getPriority()+1)
                    .doOnSuccess(stepFound -> {
                        OrderData orderData = new OrderData();
                        orderData.setOrderId(message.getPayload().getOrderId());
                        orderData.setPriority(message.getPayload().getPriority()+1);
                        orderData.setData(message.getPayload().getData());
                        log.info("Data before send message : {}", orderData.getData());
                        orderOrchestrationService.sendMessage(stepFound.getStep(), orderData);
                    }).subscribe()
        }
    }
}
