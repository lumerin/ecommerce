package com.ecommerce.orderorchestration.repository;

import com.ecommerce.orderorchestration.model.OrderStep;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface OrderStepRepository extends R2dbcRepository<OrderStep, Integer> {
    Mono<OrderStep> findByOrderIdAndPriority(Integer orderId, Integer priority);
}
