package com.ecommerce.orderorchestration.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Order {

	private  Integer orderId;

	private String action;

	private Object data;

}
