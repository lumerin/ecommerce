package com.ecommerce.orderorchestration.repository;

import com.ecommerce.orderorchestration.model.TransaksiStep;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface TransaksiStepRepository extends R2dbcRepository<TransaksiStep, Integer> {
    @Query("SELECT * FROM trx_steps WHERE order_id =:orderId AND status ='INIT' " +
            "AND priority = (select min(priority) from trx_steps where order_id=:orderId and status = 'INIT')")
    Mono<TransaksiStep> findStatus(Integer orderId);
    @Query("SELECT * FROM trx_steps WHERE order_id =:orderId AND status ='COMPLETED' " +
            "AND priority = (select max(priority) from trx_steps where order_id=:orderId and status = 'COMPLETED')")
    Mono<TransaksiStep> findByStatusRollback(Integer orderId);
    Mono<TransaksiStep> findByOrderIdAndPriority(Integer orderId, Integer priority);

}
