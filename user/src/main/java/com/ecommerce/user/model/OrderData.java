package com.ecommerce.user.model;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderData {
    Integer orderId;
    Integer priority;
    Object data;
}

