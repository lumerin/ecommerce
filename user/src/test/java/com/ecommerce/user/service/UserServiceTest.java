package com.ecommerce.user.service;

import com.ecommerce.user.model.OrderData;
import com.ecommerce.user.model.User;
import com.ecommerce.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.jms.core.JmsTemplate;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private JmsTemplate jmsTemplate;

    @Mock
    private R2dbcEntityTemplate r2dbcEntityTemplate;

    @InjectMocks
    private UserService userService;

    @Test
    void testRegister() {
        OrderData orderData = new OrderData();
        orderData.setOrderId(1);
        orderData.setPriority(1);
        Map<String, Object> userDataMap = new HashMap<>();
        userDataMap.put("name", "user");
        userDataMap.put("username", "user");
        userDataMap.put("password", "user");
        userDataMap.put("balance", 50000);
        orderData.setData(userDataMap);

        User user = new User();
        user.setId("abc123");
        user.setName(userDataMap.get("name").toString());
        user.setUsername(userDataMap.get("username").toString());
        user.setPassword(userDataMap.get("password").toString());
        user.setBalance((Integer) userDataMap.get("balance"));
        user.setStatus(true);
        user.setCreatedDate(LocalDate.now());

        lenient().when(r2dbcEntityTemplate.insert(any(User.class))).thenReturn(Mono.just(user));
        lenient().doNothing().when(jmsTemplate).convertAndSend(anyString(), (Object) any());
        userService.register(orderData);

        verify(r2dbcEntityTemplate).insert(any(User.class));
        verify(jmsTemplate).convertAndSend(anyString(), (Object) any());
    }

    @Test
    void testRegisterError() {
        OrderData orderData = new OrderData();
        orderData.setOrderId(1);
        orderData.setPriority(1);
        Map<String, Object> userDataMap = new HashMap<>();
        userDataMap.put("name", "user");
        userDataMap.put("username", "user");
        userDataMap.put("password", "user");
        userDataMap.put("balance", 50000);
        orderData.setData(userDataMap);

        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setName(userDataMap.get("name").toString());
        user.setUsername(userDataMap.get("username").toString());
        user.setPassword(userDataMap.get("password").toString());
        user.setBalance((Integer) userDataMap.get("balance"));
        user.setStatus(true);
        user.setCreatedDate(LocalDate.now());

        lenient().when(r2dbcEntityTemplate.insert(any(User.class))).thenReturn(Mono.error(new RuntimeException()));
        lenient().doNothing().when(jmsTemplate).convertAndSend(anyString(), (Object) any());
        userService.register(orderData);

        verify(r2dbcEntityTemplate).insert(any(User.class));
        verify(jmsTemplate).convertAndSend(anyString(), (Object) any());
    }

    @Test
    void testRollbackRegister() {
        OrderData orderData = new OrderData();
        orderData.setOrderId(1);
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("id", UUID.randomUUID().toString());
        dataMap.put("name", "user");
        dataMap.put("username", "user");
        dataMap.put("password", "user");
        dataMap.put("balance", 50000);
        orderData.setData(dataMap);

        User user = new User();
        user.setId(dataMap.get("id").toString());
        user.setName(dataMap.get("name").toString());
        user.setUsername(dataMap.get("username").toString());
        user.setPassword(dataMap.get("password").toString());
        user.setBalance((Integer) dataMap.get("balance"));
        user.setStatus(true);

        lenient().when(userRepository.deleteById(anyString())).thenReturn(Mono.empty());
        lenient().doNothing().when(jmsTemplate).convertAndSend(anyString(), (Object) any());

        userService.rollbackRegister(orderData);

        verify(userRepository).deleteById(anyString());
        verify(jmsTemplate).convertAndSend(anyString(), (Object) any());
    }

    @Test
    void testRollbackRegisterError() {
        OrderData orderData = new OrderData();
        orderData.setOrderId(1);
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("id", UUID.randomUUID().toString());
        dataMap.put("name", "user");
        dataMap.put("username", "user");
        dataMap.put("password", "user");
        dataMap.put("balance", 50000);
        orderData.setData(dataMap);

        User user = new User();
        user.setId(dataMap.get("id").toString());
        user.setName(dataMap.get("name").toString());
        user.setUsername(dataMap.get("username").toString());
        user.setPassword(dataMap.get("password").toString());
        user.setBalance((Integer) dataMap.get("balance"));
        user.setStatus(true);

        lenient().when(userRepository.deleteById(anyString())).thenReturn(Mono.error(new RuntimeException()));

        userService.rollbackRegister(orderData);

        verify(userRepository).deleteById(anyString());
    }
}