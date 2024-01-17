package com.ecommerce.notif.model;

import lombok.Data;

@Data
public class ResponseNotif {
    Integer orderId;
    Integer priority;
    Object data;
    String notifStatus;
}

