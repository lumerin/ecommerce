package com.ecommerce.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class UserRequestDTO {
    private Integer userId;
    private UUID orderId;
    private Double amount;
}
