package com.ecommerce.orderapi.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@Table("order_steps")
public class OrderStep {
	@Id
	private Integer id;
	@Column("action_id")
	private Integer actionId;
	private String step;
	private Integer priority;
}
