package com.ecommerce.orderapi.controller;


import com.ecommerce.orderapi.model.Order;
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
	public Mono<String> createOrder(@RequestBody Order order) {
		return orderApiService.sendMessage(order);
	}

	@GetMapping("/status")
	public Mono<String> getStatus() {
		return null;
	}
}
