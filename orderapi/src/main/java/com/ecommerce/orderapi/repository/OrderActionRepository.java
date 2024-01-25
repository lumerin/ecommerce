package com.ecommerce.orderapi.repository;

import com.ecommerce.orderapi.model.OrderAction;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface OrderActionRepository extends R2dbcRepository<OrderAction, Integer> {
    Mono<OrderAction> findByAction(String action);
}
