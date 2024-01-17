package com.ecommerce.notif.model;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotifData {
    Integer orderId;
    Integer priority;
    Object data;
}

