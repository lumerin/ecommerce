package com.ecommerce.orderorchestration.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("order_action")
public class OrderAction {
    @Id
    Integer id;
    String action;
}
