package com.ecommerce.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class NotifRequestDTO {

    private Integer userId;
    private Integer productId;
    private UUID orderId;

}
