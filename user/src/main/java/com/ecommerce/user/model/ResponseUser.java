package com.ecommerce.user.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;

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

