package com.ecommerce.orderapi.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@Table("trx_steps")
public class TransaksiStep {
	@Id
	private Integer id;
	private Integer order_id;
	private Integer action_id;
	private String step;
	private Integer priority;
	private String status;
}
