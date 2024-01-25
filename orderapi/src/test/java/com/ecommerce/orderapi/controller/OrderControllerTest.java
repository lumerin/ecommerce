package com.ecommerce.orderapi.controller;

import com.ecommerce.orderapi.model.Notif;
import com.ecommerce.orderapi.model.Order;
import com.ecommerce.orderapi.model.OrderApi;
import com.ecommerce.orderapi.service.OrderService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Mock
    private OrderService orderApiService;

    @InjectMocks
    private OrderController orderController;

    @Test
    void testCreateOrder() throws Exception {
        Order order = new Order();

        when(orderApiService.sendMessage(any(Order.class))).thenReturn(Mono.just(new OrderApi()));

        mockMvc.perform(post("/api/orders")
                        .contentType("application/json")
                        .content(String.valueOf(new OrderApi())))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
        // Verifying that the sendMessage method is called
        verify(orderApiService, times(1)).sendMessage(order);
    }

    @Test
    void testGetStatus() throws Exception {
        Integer orderId = 1;

        // Mocking the behavior of orderApiService
        when(orderApiService.getStatus(orderId)).thenReturn(Mono.just(new Notif()));

        // Performing the mockMvc request
        mockMvc.perform(get("/api/status/{orderId}", orderId))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));

        // Verifying that the getStatus method is called
        verify(orderApiService, times(1)).getStatus(orderId);
    }
}