package com.ecommerce.notif.model;

import lombok.Data;

@Data
public class ResponseUser {
    Integer orderId;
    Integer priority;
    String id;
    String name;
    String username;
    String password;
    Integer balance;
    boolean status;
    String userStatus;
}

