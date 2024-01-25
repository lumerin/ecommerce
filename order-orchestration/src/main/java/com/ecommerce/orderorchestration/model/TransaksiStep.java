package com.ecommerce.orderorchestration.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("trx_steps")
public class TransaksiStep {
    @Id
    Integer id;
    @Column("order_id")
    Integer orderId;
    @Column("action_id")
    Integer actionId;
    String step;
    Integer priority;
    String status;
}
