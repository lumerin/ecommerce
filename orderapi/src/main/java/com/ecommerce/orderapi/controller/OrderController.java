package com.ecommerce.orderapi.controller;


import com.ecommerce.orderapi.model.Notif;
import com.ecommerce.orderapi.model.Order;
import com.ecommerce.orderapi.model.OrderApi;
import com.ecommerce.orderapi.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
@Slf4j
public class OrderController {


	@Autowired
	private OrderService orderApiService;

	@PostMapping("/orders")
	public Mono<OrderApi> createOrder(@RequestBody Order order) {
		return orderApiService.sendMessage(order);
	}

	@GetMapping("/status/{orderId}")
	public Mono<Notif> getStatus(@PathVariable("orderId") Integer orderId) {
		return orderApiService.getStatus(orderId);
	}
}
