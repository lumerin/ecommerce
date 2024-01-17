package com.ecommerce.orderorchestration.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("order_steps")
public class OrderStep {
    @Id
    Integer id;
    @Column("order_id")
    Integer orderId;
    String step;
    Integer priority;
}
