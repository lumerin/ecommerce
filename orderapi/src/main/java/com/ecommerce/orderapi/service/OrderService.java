gpackage com.ecommerce.orderapi.service;


import com.ecommerce.orderapi.model.Notif;
import com.ecommerce.orderapi.model.Order;
import com.ecommerce.orderapi.model.OrderApi;
import com.ecommerce.orderapi.model.TransaksiStep;
import com.ecommerce.orderapi.repository.NotifRepository;
import com.ecommerce.orderapi.repository.OrderActionRepository;
import com.ecommerce.orderapi.repository.OrderStepRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class OrderService {
    final JmsTemplate jmsTemplate;
    final R2dbcEntityTemplate r2dbcEntityTemplate;
    final OrderStepRepository orderStepRepository;
    final OrderActionRepository orderActionRepository;
    final NotifRepository notifRepository;

    @Autowired
    public OrderService(JmsTemplate jmsTemplate, R2dbcEntityTemplate r2dbcEntityTemplate, OrderStepRepository orderStepRepository, OrderActionRepository orderActionRepository, NotifRepository notifRepository) {
        this.jmsTemplate = jmsTemplate;
        this.r2dbcEntityTemplate = r2dbcEntityTemplate;
        this.orderStepRepository = orderStepRepository;
        this.orderActionRepository = orderActionRepository;
        this.notifRepository = notifRepository;
    }

    public Mono<OrderApi> sendMessage(Order order) {
        try {
            OrderApi orderApi = new OrderApi();
            orderApi.setAction(order.getAction());
            return r2dbcEntityTemplate.insert(orderApi)
                .doOnSuccess(orderSaved -> {
                    log.info("aaaaaaaaaaa : {}", orderSaved);
                    log.info("aaaaaaaaaaa : {}", orderSaved.getId());
                    log.info("aaaaaaaaaaa : {}", orderSaved.getAction());
                    orderActionRepository.findByAction(orderSaved.getAction())
                        .doOnSuccess(action -> {
                            log.info("asdasd : {}", action.getAction());
                            orderStepRepository.findAllByActionIdOrderByPriorityAsc(action.getId())
                                .subscribe(steps -> {
                                    TransaksiStep transaksiStep = new TransaksiStep();
                                    transaksiStep.setOrder_id(orderSaved.getId());
                                    transaksiStep.setAction_id(action.getId());
                                    transaksiStep.setStep(steps.getStep());
                                    transaksiStep.setPriority(steps.getPriority());
                                    transaksiStep.setStatus("INIT");
                                    r2dbcEntityTemplate.insert(transaksiStep).subscribe();
                                });
                            order.setOrderId(orderSaved.getId());
                            jmsTemplate.convertAndSend("queue.orders", order);
                        })
                        .doOnError(err -> {
                            log.info("err");
                        }).subscribe();
                });
        } catch (Exception e) {
            log.error("Error sendMessage try catch : {}", e.getMessage());
            return Mono.error(e.getCause());
        }
    }
    public Mono<Notif> getStatus(Integer orderId) {
        return notifRepository.findByOrderId(orderId)
                .doOnSuccess(found -> log.info("get status order : {}", found));
    }
}
