package com.ecommerce.orderapi.repository;

import com.ecommerce.orderapi.model.Notif;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface NotifRepository extends R2dbcRepository<Notif, Integer> {
    Mono<Notif> findByOrderId(Integer orderId);
}
