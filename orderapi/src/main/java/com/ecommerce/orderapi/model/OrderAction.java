package com.ecommerce.orderapi.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@Table("order_action")
public class OrderAction {
	@Id
	private Integer id;

	private String action;

}
