package com.ecommerce.orderorchestration.service;

import com.ecommerce.orderorchestration.model.*;
import com.ecommerce.orderorchestration.repository.TransaksiStepRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import reactor.core.publisher.Mono;


import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsumerServiceTest {
    @Mock
    private TransaksiStepRepository transaksiStepRepository;

    @Mock
    private OrderOrchestrationService orderOrchestrationService;

    @InjectMocks
    private ConsumerService consumerService;

    @Test
    void testProcessOrder() {
        Order order = new Order();
        order.setOrderId(1);
        order.setAction("REGISTER");
        order.setData("data");

        Message<Order> message = mock(Message.class);
        when(message.getPayload()).thenReturn(order);

        TransaksiStep transaksiStep = new TransaksiStep();
        transaksiStep.setId(1);
        transaksiStep.setOrderId(1);
        transaksiStep.setActionId(1);
        transaksiStep.setStep("queue.user.register");
        transaksiStep.setPriority(0);
        transaksiStep.setStatus("INIT");

        when(transaksiStepRepository.findStatus(order.getOrderId())).thenReturn(Mono.just(transaksiStep));
        doNothing().when(orderOrchestrationService).sendMessage(anyString(), any(OrderData.class));

        consumerService.processOrder(message);

        verify(transaksiStepRepository).findStatus(order.getOrderId());
        verify(orderOrchestrationService).sendMessage(anyString(), any(OrderData.class));
    }

    @Test
    void testStatusRegisterUserApproved() {
        ResponseUser responseUser = new ResponseUser();
        responseUser.setOrderId(1);
        responseUser.setPriority(0);
        responseUser.setId("abc123");
        responseUser.setName("user");
        responseUser.setUsername("user");
        responseUser.setPassword("user");
        responseUser.setBalance(50000);
        responseUser.setStatus(true);
        responseUser.setUserStatus("USER_APPROVED");

        Message<ResponseUser> message = mock(Message.class);
        when(message.getPayload()).thenReturn(responseUser);

        TransaksiStep transaksiStep = new TransaksiStep();
        transaksiStep.setId(1);
        transaksiStep.setActionId(1);
        transaksiStep.setOrderId(1);
        transaksiStep.setStep("queue.user.register");
        transaksiStep.setPriority(0);
        transaksiStep.setStatus("INIT");

        TransaksiStep transaksiUpdateStep = new TransaksiStep();
        transaksiUpdateStep.setId(1);
        transaksiUpdateStep.setActionId(1);
        transaksiUpdateStep.setOrderId(1);
        transaksiUpdateStep.setStep("queue.user.register");
        transaksiUpdateStep.setPriority(0);
        transaksiUpdateStep.setStatus("COMPLETED");

        TransaksiStep transaksiNextStep = new TransaksiStep();
        transaksiNextStep.setId(1);
        transaksiNextStep.setActionId(1);
        transaksiNextStep.setOrderId(1);
        transaksiNextStep.setStep("queue.notif");
        transaksiNextStep.setPriority(1);
        transaksiNextStep.setStatus("INIT");

        when(transaksiStepRepository.findStatus(any())).thenReturn(Mono.just(transaksiStep));
        when(transaksiStepRepository.save(transaksiUpdateStep)).thenReturn(Mono.just(transaksiUpdateStep));
        doNothing().when(orderOrchestrationService).sendMessage(anyString(), any(OrderData.class));

        consumerService.statusRegister(message);

        verify(transaksiStepRepository, times(2)).findStatus(any());
        verify(transaksiStepRepository).save(transaksiUpdateStep);
        verify(orderOrchestrationService).sendMessage(anyString(), any(OrderData.class));
    }

    @Test
    void testStatusRegisterUserRejected() {
        ResponseUser responseUser = new ResponseUser();
        responseUser.setUserStatus("USER_REJECTED");
        responseUser.setOrderId(1);
        responseUser.setPriority(0);

        Message<ResponseUser> message = mock(Message.class);
        when(message.getPayload()).thenReturn(responseUser);

        TransaksiStep transaksiStep = new TransaksiStep();
        transaksiStep.setId(1);
        transaksiStep.setActionId(1);
        transaksiStep.setOrderId(1);
        transaksiStep.setStep("queue.user.register");
        transaksiStep.setPriority(0);
        transaksiStep.setStatus("INIT");

        TransaksiStep transaksiUpdateStep = new TransaksiStep();
        transaksiUpdateStep.setId(1);
        transaksiUpdateStep.setActionId(1);
        transaksiUpdateStep.setOrderId(1);
        transaksiUpdateStep.setStep("queue.user.register");
        transaksiUpdateStep.setPriority(0);
        transaksiUpdateStep.setStatus("CANCELED");

        TransaksiStep transaksiRollbackStep = new TransaksiStep();
        transaksiRollbackStep.setId(1);
        transaksiRollbackStep.setActionId(1);
        transaksiRollbackStep.setOrderId(1);
        transaksiRollbackStep.setStep("queue.user.register");
        transaksiRollbackStep.setPriority(0);
        transaksiRollbackStep.setStatus("CANCELED");

        when(transaksiStepRepository.findByOrderIdAndPriority(1, 0)).thenReturn(Mono.just(transaksiStep));
        when(transaksiStepRepository.save(transaksiUpdateStep)).thenReturn(Mono.just(transaksiUpdateStep));
        when(transaksiStepRepository.findByStatusRollback(1)).thenReturn(Mono.just(transaksiRollbackStep));
        doNothing().when(orderOrchestrationService).sendMessage(anyString(), any(OrderData.class));

        consumerService.statusRegister(message);

        verify(transaksiStepRepository).findByOrderIdAndPriority(1, 0);
        verify(transaksiStepRepository).save(transaksiUpdateStep);
        verify(transaksiStepRepository).findByStatusRollback(1);
        verify(orderOrchestrationService).sendMessage(anyString(), any(OrderData.class));
    }

    @Test
    void testStatusRegisterUserRejectedNull() {
        ResponseUser responseUser = new ResponseUser();
        responseUser.setUserStatus("USER_REJECTED");
        responseUser.setOrderId(1);
        responseUser.setPriority(0);

        Message<ResponseUser> message = mock(Message.class);
        when(message.getPayload()).thenReturn(responseUser);

        TransaksiStep transaksiStep = new TransaksiStep();
        transaksiStep.setId(1);
        transaksiStep.setActionId(1);
        transaksiStep.setOrderId(1);
        transaksiStep.setStep("queue.user.register");
        transaksiStep.setPriority(0);
        transaksiStep.setStatus("INIT");

        TransaksiStep transaksiUpdateStep = new TransaksiStep();
        transaksiUpdateStep.setId(1);
        transaksiUpdateStep.setActionId(1);
        transaksiUpdateStep.setOrderId(1);
        transaksiUpdateStep.setStep("queue.user.register");
        transaksiUpdateStep.setPriority(0);
        transaksiUpdateStep.setStatus("CANCELED");

        TransaksiStep transaksiRollbackStep = new TransaksiStep();
        transaksiRollbackStep.setId(1);
        transaksiRollbackStep.setActionId(1);
        transaksiRollbackStep.setOrderId(1);
        transaksiRollbackStep.setStep("queue.user.register");
        transaksiRollbackStep.setPriority(0);
        transaksiRollbackStep.setStatus("CANCELED");

        when(transaksiStepRepository.findByOrderIdAndPriority(1, 0)).thenReturn(Mono.just(transaksiStep));
        when(transaksiStepRepository.save(transaksiUpdateStep)).thenReturn(Mono.just(transaksiUpdateStep));
        when(transaksiStepRepository.findByStatusRollback(1)).thenReturn(Mono.empty());

        consumerService.statusRegister(message);

        verify(transaksiStepRepository).findByOrderIdAndPriority(1, 0);
        verify(transaksiStepRepository).save(transaksiUpdateStep);
        verify(transaksiStepRepository).findByStatusRollback(1);
    }

    @Test
    void testRollbackRegister() {
        ResponseUser responseUser = new ResponseUser();
        responseUser.setUserStatus("ROLLBACKED");
        responseUser.setOrderId(1);
        responseUser.setPriority(0);

        Message<ResponseUser> message = mock(Message.class);
        when(message.getPayload()).thenReturn(responseUser);

        TransaksiStep transaksiStep = new TransaksiStep();
        transaksiStep.setId(1);
        transaksiStep.setActionId(1);
        transaksiStep.setOrderId(1);
        transaksiStep.setStep("queue.user.register");
        transaksiStep.setPriority(0);
        transaksiStep.setStatus("COMPLETED");

        TransaksiStep transaksiUpdateStep = new TransaksiStep();
        transaksiUpdateStep.setId(1);
        transaksiUpdateStep.setActionId(1);
        transaksiUpdateStep.setOrderId(1);
        transaksiUpdateStep.setStep("queue.user.register");
        transaksiUpdateStep.setPriority(0);
        transaksiUpdateStep.setStatus("CANCELED");

        TransaksiStep transaksiNextRollbackStep = new TransaksiStep();
        transaksiNextRollbackStep.setId(1);
        transaksiNextRollbackStep.setActionId(1);
        transaksiNextRollbackStep.setOrderId(1);
        transaksiNextRollbackStep.setStep("queue.user.register");
        transaksiNextRollbackStep.setPriority(0);
        transaksiNextRollbackStep.setStatus("COMPLETED");

        when(transaksiStepRepository.findByOrderIdAndPriority(1, 0)).thenReturn(Mono.just(transaksiStep));
        when(transaksiStepRepository.save(transaksiUpdateStep)).thenReturn(Mono.just(transaksiUpdateStep));
        when(transaksiStepRepository.findByStatusRollback(1)).thenReturn(Mono.just(transaksiNextRollbackStep));
        doNothing().when(orderOrchestrationService).sendMessage(anyString(), any(OrderData.class));

        consumerService.rollbackRegister(message);

        verify(transaksiStepRepository).findByOrderIdAndPriority(1, 0);
        verify(transaksiStepRepository).save(transaksiUpdateStep);
        verify(transaksiStepRepository).findByStatusRollback(1);
        verify(orderOrchestrationService).sendMessage(anyString(), any(OrderData.class));
    }

    @Test
    void testRollbackRegisterNextNull() {
        ResponseUser responseUser = new ResponseUser();
        responseUser.setUserStatus("ROLLBACKED");
        responseUser.setOrderId(1);
        responseUser.setPriority(0);

        Message<ResponseUser> message = mock(Message.class);
        when(message.getPayload()).thenReturn(responseUser);

        TransaksiStep transaksiStep = new TransaksiStep();
        transaksiStep.setId(1);
        transaksiStep.setActionId(1);
        transaksiStep.setOrderId(1);
        transaksiStep.setStep("queue.user.register");
        transaksiStep.setPriority(0);
        transaksiStep.setStatus("COMPLETED");

        TransaksiStep transaksiUpdateStep = new TransaksiStep();
        transaksiUpdateStep.setId(1);
        transaksiUpdateStep.setActionId(1);
        transaksiUpdateStep.setOrderId(1);
        transaksiUpdateStep.setStep("queue.user.register");
        transaksiUpdateStep.setPriority(0);
        transaksiUpdateStep.setStatus("CANCELED");

        TransaksiStep transaksiNextRollbackStep = new TransaksiStep();
        transaksiNextRollbackStep.setId(1);
        transaksiNextRollbackStep.setActionId(1);
        transaksiNextRollbackStep.setOrderId(1);
        transaksiNextRollbackStep.setStep("queue.user.register");
        transaksiNextRollbackStep.setPriority(0);
        transaksiNextRollbackStep.setStatus("COMPLETED");

        when(transaksiStepRepository.findByOrderIdAndPriority(1, 0)).thenReturn(Mono.just(transaksiStep));
        when(transaksiStepRepository.save(transaksiUpdateStep)).thenReturn(Mono.just(transaksiUpdateStep));
        when(transaksiStepRepository.findByStatusRollback(1)).thenReturn(Mono.empty());

        consumerService.rollbackRegister(message);

        verify(transaksiStepRepository).findByOrderIdAndPriority(1, 0);
        verify(transaksiStepRepository).save(transaksiUpdateStep);
        verify(transaksiStepRepository).findByStatusRollback(1);
    }

    @Test
    void testStatusNotifCompleted() {
        ResponseNotif responseNotif = new ResponseNotif();
        responseNotif.setNotifStatus("NOTIF_COMPLETED");
        responseNotif.setOrderId(1);
        responseNotif.setPriority(1);
        responseNotif.setData("data");

        Message<ResponseNotif> message = mock(Message.class);
        when(message.getPayload()).thenReturn(responseNotif);

        TransaksiStep transaksiStep = new TransaksiStep();
        transaksiStep.setId(2);
        transaksiStep.setActionId(1);
        transaksiStep.setOrderId(1);
        transaksiStep.setStep("queue.notif");
        transaksiStep.setPriority(1);
        transaksiStep.setStatus("INIT");

        TransaksiStep transaksiUpdateStep = new TransaksiStep();
        transaksiUpdateStep.setId(2);
        transaksiUpdateStep.setActionId(1);
        transaksiUpdateStep.setOrderId(1);
        transaksiUpdateStep.setStep("queue.notif");
        transaksiUpdateStep.setPriority(1);
        transaksiUpdateStep.setStatus("COMPLETED");

        TransaksiStep transaksiNextStep = new TransaksiStep();
        transaksiNextStep.setId(2);
        transaksiNextStep.setActionId(1);
        transaksiNextStep.setOrderId(1);
        transaksiNextStep.setStep("queue.complete");
        transaksiNextStep.setPriority(1);
        transaksiNextStep.setStatus("INIT");

        when(transaksiStepRepository.findStatus(1)).thenReturn(Mono.just(transaksiStep));
        when(transaksiStepRepository.save(transaksiUpdateStep)).thenReturn(Mono.just(transaksiUpdateStep));
        doNothing().when(orderOrchestrationService).sendMessage(anyString(), any(OrderData.class));

        consumerService.statusNotif(message);

        verify(transaksiStepRepository, times(2)).findStatus(1);
        verify(transaksiStepRepository).save(transaksiUpdateStep);
        verify(orderOrchestrationService).sendMessage(anyString(), any(OrderData.class));
    }

    @Test
    void testStatusNotifRejected() {
        ResponseNotif responseNotif = new ResponseNotif();
        responseNotif.setNotifStatus("NOTIF_REJECTED");
        responseNotif.setOrderId(1);
        responseNotif.setPriority(1);
        responseNotif.setData("data");

        Message<ResponseNotif> message = mock(Message.class);
        when(message.getPayload()).thenReturn(responseNotif);

        TransaksiStep transaksiStep = new TransaksiStep();
        transaksiStep.setId(2);
        transaksiStep.setActionId(1);
        transaksiStep.setOrderId(1);
        transaksiStep.setStep("queue.notif");
        transaksiStep.setPriority(1);
        transaksiStep.setStatus("INIT");

        TransaksiStep transaksiUpdateStep = new TransaksiStep();
        transaksiUpdateStep.setId(2);
        transaksiUpdateStep.setActionId(1);
        transaksiUpdateStep.setOrderId(1);
        transaksiUpdateStep.setStep("queue.notif");
        transaksiUpdateStep.setPriority(1);
        transaksiUpdateStep.setStatus("CANCELED");

        TransaksiStep transaksiRollbackStep = new TransaksiStep();
        transaksiRollbackStep.setId(1);
        transaksiRollbackStep.setActionId(1);
        transaksiRollbackStep.setOrderId(1);
        transaksiRollbackStep.setStep("queue.complete");
        transaksiRollbackStep.setPriority(0);
        transaksiRollbackStep.setStatus("COMPLETED");

        when(transaksiStepRepository.findByOrderIdAndPriority(1, 1)).thenReturn(Mono.just(transaksiStep));
        when(transaksiStepRepository.save(transaksiUpdateStep)).thenReturn(Mono.just(transaksiUpdateStep));
        when(transaksiStepRepository.findByStatusRollback(1)).thenReturn(Mono.just(transaksiRollbackStep));
        doNothing().when(orderOrchestrationService).sendMessage(anyString(), any(OrderData.class));

        consumerService.statusNotif(message);

        verify(transaksiStepRepository).findByOrderIdAndPriority(1, 1);
        verify(transaksiStepRepository).save(transaksiUpdateStep);
        verify(transaksiStepRepository).findByStatusRollback(1);
        verify(orderOrchestrationService).sendMessage(anyString(), any(OrderData.class));
    }

    @Test
    void testStatusNotifRejectedNextRollbackNull() {
        ResponseNotif responseNotif = new ResponseNotif();
        responseNotif.setNotifStatus("NOTIF_REJECTED");
        responseNotif.setOrderId(1);
        responseNotif.setPriority(1);
        responseNotif.setData("data");

        Message<ResponseNotif> message = mock(Message.class);
        when(message.getPayload()).thenReturn(responseNotif);

        TransaksiStep transaksiStep = new TransaksiStep();
        transaksiStep.setId(2);
        transaksiStep.setActionId(1);
        transaksiStep.setOrderId(1);
        transaksiStep.setStep("queue.notif");
        transaksiStep.setPriority(1);
        transaksiStep.setStatus("INIT");

        TransaksiStep transaksiUpdateStep = new TransaksiStep();
        transaksiUpdateStep.setId(2);
        transaksiUpdateStep.setActionId(1);
        transaksiUpdateStep.setOrderId(1);
        transaksiUpdateStep.setStep("queue.notif");
        transaksiUpdateStep.setPriority(1);
        transaksiUpdateStep.setStatus("CANCELED");

        TransaksiStep transaksiRollbackStep = new TransaksiStep();
        transaksiRollbackStep.setId(1);
        transaksiRollbackStep.setActionId(1);
        transaksiRollbackStep.setOrderId(1);
        transaksiRollbackStep.setStep("queue.complete");
        transaksiRollbackStep.setPriority(0);
        transaksiRollbackStep.setStatus("COMPLETED");

        when(transaksiStepRepository.findByOrderIdAndPriority(1, 1)).thenReturn(Mono.just(transaksiStep));
        when(transaksiStepRepository.save(transaksiUpdateStep)).thenReturn(Mono.just(transaksiUpdateStep));
        when(transaksiStepRepository.findByStatusRollback(1)).thenReturn(Mono.empty());

        consumerService.statusNotif(message);

        verify(transaksiStepRepository).findByOrderIdAndPriority(1, 1);
        verify(transaksiStepRepository).save(transaksiUpdateStep);
        verify(transaksiStepRepository).findByStatusRollback(1);
    }
}