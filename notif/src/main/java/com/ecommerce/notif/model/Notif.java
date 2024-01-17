package com.ecommerce.notif.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;

@Table(name = "notifs")
@Data
public class Notif {
    @Id
    String id;
    @Column("order_id")
    Integer orderId;
    String status;
    @Column("created_date")
    LocalDate createdDate;
}

