package com.ecommerce.orderapi.repository;

import com.ecommerce.orderapi.model.OrderStep;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface OrderStepRepository extends R2dbcRepository<OrderStep, Integer> {
    Flux<OrderStep> findAllByActionIdOrderByPriorityAsc(Integer actionId);
}
