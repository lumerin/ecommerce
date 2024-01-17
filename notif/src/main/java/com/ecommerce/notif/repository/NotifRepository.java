package com.ecommerce.notif.repository;

import com.ecommerce.notif.model.Notif;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotifRepository extends R2dbcRepository<Notif, String> {

}
