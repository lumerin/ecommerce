package com.ecommerce.orderorchestration.model;

import lombok.Data;

@Data
public class ResponseNotif {
    Integer orderId;
    Integer priority;
    Object data;
    String notifStatus;
}

