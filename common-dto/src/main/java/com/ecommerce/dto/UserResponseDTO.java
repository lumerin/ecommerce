package com.ecommerce.dto;


import com.ecommerce.enums.UserStatus;
import lombok.Data;

import java.util.UUID;

@Data
public class UserResponseDTO {
    private Integer userId;
    private UUID orderId;
    private Double amount;
    private UserStatus status;
}
