package com.ecommerce.orderapi.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;

@Getter
@Setter
@Table("notifs")
public class Notif {
	@Id
	private Integer id;

	@Column("order_id")
	private Integer orderId;

	private String status;

	@Column("created_date")
	private LocalDate createdDate;

}
