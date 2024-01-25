package com.ecommerce.orderapi.service;

import com.ecommerce.orderapi.model.*;
import com.ecommerce.orderapi.repository.NotifRepository;
import com.ecommerce.orderapi.repository.OrderActionRepository;
import com.ecommerce.orderapi.repository.OrderStepRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.jms.core.JmsTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @Mock
    private JmsTemplate jmsTemplate;

    @Mock
    private R2dbcEntityTemplate r2dbcEntityTemplate;

    @Mock
    private OrderStepRepository orderStepRepository;

    @Mock
    private OrderActionRepository orderActionRepository;

    @Mock
    private NotifRepository notifRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    public void testSendMessage() {
        Order order = new Order();
        order.setAction("REGISTER");

        OrderApi orderApi = new OrderApi();
        orderApi.setId(1);
        orderApi.setAction(order.getAction());

        OrderAction orderAction = new OrderAction();
        orderAction.setId(1);
        orderAction.setAction(order.getAction());

        OrderStep orderStep1 = new OrderStep();
        orderStep1.setId(1);
        orderStep1.setActionId(1);
        orderStep1.setStep("queue.user.register");
        orderStep1.setPriority(0);

        OrderStep orderStep2 = new OrderStep();
        orderStep2.setId(2);
        orderStep2.setActionId(1);
        orderStep2.setStep("queue.notif");
        orderStep2.setPriority(1);

        OrderStep orderStep3 = new OrderStep();
        orderStep3.setId(1);
        orderStep3.setActionId(1);
        orderStep3.setStep("queue.complete");
        orderStep3.setPriority(0);

        TransaksiStep transaksiStep = new TransaksiStep();
        transaksiStep.setId(1);
        transaksiStep.setOrder_id(1);
        transaksiStep.setAction_id(1);
        transaksiStep.setStep("queue.user.register");
        transaksiStep.setPriority(0);
        transaksiStep.setStatus("INIT");

        when(r2dbcEntityTemplate.insert(Mockito.any(OrderApi.class))).thenReturn(Mono.just(orderApi));
        when(orderActionRepository.findByAction(order.getAction())).thenReturn(Mono.just(orderAction));
        when(orderStepRepository.findAllByActionIdOrderByPriorityAsc(orderAction.getId())).thenReturn(Flux.just(orderStep1,orderStep2, orderStep3));
        when(r2dbcEntityTemplate.insert(transaksiStep)).thenReturn(Mono.just(transaksiStep));

        StepVerifier.create(orderService.sendMessage(order))
                .expectNextCount(1)
                .verifyComplete();

        Mockito.verify(jmsTemplate, times(1)).convertAndSend(eq("queue.orders"), eq(order));
    }

    @Test
    public void testSendMessageGetActionError() {
        Order order = new Order();
        order.setAction("REGISTER");

        OrderApi orderApi = new OrderApi();
        orderApi.setId(1);
        orderApi.setAction(order.getAction());

        OrderAction orderAction = new OrderAction();
        orderAction.setId(1);
        orderAction.setAction(order.getAction());

        OrderStep orderStep1 = new OrderStep();
        orderStep1.setId(1);
        orderStep1.setActionId(1);
        orderStep1.setStep("queue.user.register");
        orderStep1.setPriority(0);

        OrderStep orderStep2 = new OrderStep();
        orderStep2.setId(2);
        orderStep2.setActionId(1);
        orderStep2.setStep("queue.notif");
        orderStep2.setPriority(1);

        OrderStep orderStep3 = new OrderStep();
        orderStep3.setId(1);
        orderStep3.setActionId(1);
        orderStep3.setStep("queue.complete");
        orderStep3.setPriority(0);

        TransaksiStep transaksiStep = new TransaksiStep();
        transaksiStep.setId(1);
        transaksiStep.setOrder_id(1);
        transaksiStep.setAction_id(1);
        transaksiStep.setStep("queue.user.register");
        transaksiStep.setPriority(0);
        transaksiStep.setStatus("INIT");

        when(r2dbcEntityTemplate.insert(Mockito.any(OrderApi.class))).thenReturn(Mono.just(orderApi));
        when(orderActionRepository.findByAction(order.getAction())).thenReturn(Mono.error(new RuntimeException()));

        StepVerifier.create(orderService.sendMessage(order))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    public void testSendMessageWithError() {
        Order order = new Order();
        order.setAction("someAction");

        Mockito.when(r2dbcEntityTemplate.insert(order))
                .thenThrow(new RuntimeException());
        orderService.sendMessage(order).subscribe();

        Mockito.verify(orderService, Mockito.times(1)).sendMessage(order);
    }

    @Test
    public void testGetStatus() {
        int orderId = 1;

        Notif notif = new Notif();
        notif.setOrderId(orderId);

        when(notifRepository.findByOrderId(orderId))
                .thenReturn(Mono.just(notif));

        StepVerifier.create(orderService.getStatus(orderId))
                .expectNext(notif)
                .verifyComplete();
    }
}