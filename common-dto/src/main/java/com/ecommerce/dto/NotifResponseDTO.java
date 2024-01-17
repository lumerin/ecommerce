package com.ecommerce.dto;

import com.ecommerce.enums.NotifStatus;
import lombok.Data;

import java.util.UUID;

@Data
public class NotifResponseDTO {

    private UUID orderId;
    private Integer userId;
    private Integer productId;
    private NotifStatus status;

}
