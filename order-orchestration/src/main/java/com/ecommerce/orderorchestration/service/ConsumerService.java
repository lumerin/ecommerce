tpackage com.ecommerce.orderorchestration.service;

import com.ecommerce.orderorchestration.model.*;
import com.ecommerce.orderorchestration.repository.TransaksiStepRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("ALL")
@Service
@Slf4j
public class ConsumerService {

    final OrderOrchestrationService orderOrchestrationService;
    final TransaksiStepRepository transaksiStepRepository;

    @Autowired
    public ConsumerService(OrderOrchestrationService orderOrchestrationService, TransaksiStepRepository transaksiStepRepository) {
        this.orderOrchestrationService = orderOrchestrationService;
        this.transaksiStepRepository = transaksiStepRepository;
    }


    @JmsListener(destination = "queue.orders")
    public void processOrder(Message<Order> message) {
        Order order = new Order();
        order.setOrderId(message.getPayload().getOrderId());
        order.setAction(message.getPayload().getAction());
        order.setData(message.getPayload().getData());
        log.info("order masuk dengan data payload : {} {} {}", order.getOrderId(), order.getAction(), order.getData());
        transaksiStepRepository.findStatus(order.getOrderId())
            .doOnSuccess(stepFound -> {
                OrderData orderData = new OrderData();
                orderData.setOrderId(stepFound.getOrderId());
                orderData.setPriority(stepFound.getPriority());
                orderData.setData(message.getPayload().getData());
                log.info("(First Step) Data before send message : {}", orderData.getData());
                orderOrchestrationService.sendMessage(stepFound.getStep(), orderData);
            }).subscribe();
    }

    @JmsListener(destination = "queue.user.register.status")
    public void statusRegister(Message<ResponseUser> message) {
        if (message.getPayload().getUserStatus().equals("USER_APPROVED")) {
            transaksiStepRepository.findStatus(message.getPayload().getOrderId())
                .doOnSuccess(found -> {
                    TransaksiStep transaksiStep = new TransaksiStep();
                    transaksiStep.setId(found.getId());
                    transaksiStep.setActionId(found.getActionId());
                    transaksiStep.setOrderId(found.getOrderId());
                    transaksiStep.setStep(found.getStep());
                    transaksiStep.setPriority(found.getPriority());
                    transaksiStep.setStatus("COMPLETED");
                    transaksiStepRepository.save(transaksiStep)
                            .doOnSuccess(updatedStatusTransaksi -> {
                                transaksiStepRepository.findStatus(updatedStatusTransaksi.getOrderId())
                                    .doOnSuccess(nextStepTransaksi -> {

                                        Map<String, Object> data = new HashMap<>();
                                        data.put("id", message.getPayload().getId());
                                        data.put("name", message.getPayload().getName());
                                        data.put("username", message.getPayload().getUsername());
                                        data.put("password", message.getPayload().getPassword());
                                        data.put("balance", message.getPayload().getBalance());
                                        data.put("status", message.getPayload().isStatus());

                                        OrderData orderData = new OrderData();
                                        orderData.setOrderId(nextStepTransaksi.getOrderId());
                                        orderData.setPriority(nextStepTransaksi.getPriority());
                                        orderData.setData(data);
                                        log.info("(Status Register) Data before send message : {}", orderData.getData());
                                        orderOrchestrationService.sendMessage(nextStepTransaksi.getStep(), orderData);
                                    }).subscribe();
                            }).subscribe();
                }).subscribe();
        } else {
            ResponseUser responseUser = message.getPayload();
            log.info("User rejected : {}", message.getPayload().getUserStatus());
            transaksiStepRepository.findByOrderIdAndPriority(responseUser.getOrderId(), responseUser.getPriority())
                    .doOnSuccess(found -> {
                    TransaksiStep transaksiStep = new TransaksiStep();
                    transaksiStep.setId(found.getId());
                    transaksiStep.setActionId(found.getActionId());
                    transaksiStep.setOrderId(found.getOrderId());
                    transaksiStep.setStep(found.getStep());
                    transaksiStep.setPriority(found.getPriority());
                    transaksiStep.setStatus("CANCELED");
                    transaksiStepRepository.save(transaksiStep)
                        .doOnSuccess(updatedStatus -> {
                            transaksiStepRepository.findByStatusRollback(message.getPayload().getOrderId())
                                .doOnSuccess(nextRollbackStep -> {
                                    if(nextRollbackStep == null) {
                                        log.info("Rollback completed");
                                    } else {
                                        OrderData orderData = new OrderData();
                                        orderData.setOrderId(nextRollbackStep.getOrderId());
                                        orderData.setPriority(nextRollbackStep.getPriority());
                                        orderData.setData(message.getPayload().getId());
                                        log.info("(Status Notif) Data rollback before send message : {}", orderData.getData());
                                        orderOrchestrationService.sendMessage(nextRollbackStep.getStep()+".rollback", orderData);
                                    }
                                }).subscribe();
                        }).subscribe();
                }).subscribe();
        }
    }

    @JmsListener(destination = "queue.user.register.rollback.status")
    public void rollbackRegister(Message<ResponseUser> message) {
        if (message.getPayload().getUserStatus().equals("ROLLBACKED")) {
            ResponseUser responseUser = message.getPayload();
            transaksiStepRepository.findByOrderIdAndPriority(responseUser.getOrderId(), responseUser.getPriority())
                    .doOnSuccess(found -> {
                    TransaksiStep transaksiStep = new TransaksiStep();
                    transaksiStep.setId(found.getId());
                    transaksiStep.setActionId(found.getActionId());
                    transaksiStep.setOrderId(found.getOrderId());
                    transaksiStep.setStep(found.getStep());
                    transaksiStep.setPriority(found.getPriority());
                    transaksiStep.setStatus("CANCELED");
                    transaksiStepRepository.save(transaksiStep)
                        .doOnSuccess(updatedStatus -> {
                            transaksiStepRepository.findByStatusRollback(message.getPayload().getOrderId())
                                .doOnSuccess(nextRollbackStep -> {
                                    if(nextRollbackStep == null) {
                                        log.info("Rollback completed");
                                    } else {
                                        OrderData orderData = new OrderData();
                                        orderData.setOrderId(nextRollbackStep.getOrderId());
                                        orderData.setPriority(nextRollbackStep.getPriority());
                                        orderData.setData(message.getPayload().getId());
                                        log.info("(Status User Rollback) Data rollback before send message : {}", orderData.getData());
                                        orderOrchestrationService.sendMessage(nextRollbackStep.getStep()+".rollback", orderData);
                                    }
                                }).subscribe();
                        }).subscribe();
                }).subscribe();
        }
    }

    @JmsListener(destination = "queue.notif.status")
    public void statusNotif(Message<ResponseNotif> message) {
        if (message.getPayload().getNotifStatus().equals("NOTIF_COMPLETED")) {
            transaksiStepRepository.findStatus(message.getPayload().getOrderId())
                .doOnSuccess(found -> {
                    TransaksiStep transaksiStep = new TransaksiStep();
                    transaksiStep.setId(found.getId());
                    transaksiStep.setActionId(found.getActionId());
                    transaksiStep.setOrderId(found.getOrderId());
                    transaksiStep.setStep(found.getStep());
                    transaksiStep.setPriority(found.getPriority());
                    transaksiStep.setStatus("COMPLETED");
                    transaksiStepRepository.save(transaksiStep)
                        .doOnSuccess(updatedStatusTransaksi -> transaksiStepRepository.findStatus(message.getPayload().getOrderId())
                            .doOnSuccess(nextStepTransaksi -> {
                                OrderData orderData = new OrderData();
                                orderData.setOrderId(nextStepTransaksi.getOrderId());
                                orderData.setPriority(nextStepTransaksi.getPriority());
                                orderData.setData(message.getPayload().getData());
                                log.info("(Status Notif) Data before send message : {}", orderData.getData());
                                orderOrchestrationService.sendMessage(nextStepTransaksi.getStep(), orderData);
                            }).subscribe()).subscribe();
                }).subscribe();
            TransaksiStep transaksiStep = new TransaksiStep();
            transaksiStep.setStatus("COMPLETED");
        } else {
            log.info("Notif rejected : {}", message.getPayload().getNotifStatus());
            ResponseNotif responseNotif = new ResponseNotif();
            responseNotif.setOrderId(message.getPayload().getOrderId());
            responseNotif.setPriority(message.getPayload().getPriority());
            responseNotif.setData(message.getPayload().getData());
            responseNotif.setNotifStatus(message.getPayload().getNotifStatus());
            transaksiStepRepository.findByOrderIdAndPriority(responseNotif.getOrderId(), responseNotif.getPriority())
                .doOnSuccess(found -> {
                    TransaksiStep transaksiStep = new TransaksiStep();
                    transaksiStep.setId(found.getId());
                    transaksiStep.setActionId(found.getActionId());
                    transaksiStep.setOrderId(found.getOrderId());
                    transaksiStep.setStep(found.getStep());
                    transaksiStep.setPriority(found.getPriority());
                    transaksiStep.setStatus("CANCELED");
                    transaksiStepRepository.save(transaksiStep)
                        .doOnSuccess(updatedStatus -> {
                            transaksiStepRepository.findByStatusRollback(message.getPayload().getOrderId())
                                .doOnSuccess(nextRollbackStep -> {
                                    if(nextRollbackStep == null) {
                                        log.info("Rollback completed");
                                    } else {
                                        OrderData orderData = new OrderData();
                                        orderData.setOrderId(nextRollbackStep.getOrderId());
                                        orderData.setPriority(nextRollbackStep.getPriority());
                                        orderData.setData(message.getPayload().getData());
                                        log.info("(Status Notif) Data rollback before send message : {}", orderData.getData());
                                        orderOrchestrationService.sendMessage(nextRollbackStep.getStep()+".rollback", orderData);
                                    }
                                }).subscribe();
                        }).subscribe();
                }).subscribe();
        }
    }

}
