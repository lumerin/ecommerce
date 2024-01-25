    package com.ecommerce.user.service;

    import com.ecommerce.user.model.OrderData;
    import com.ecommerce.user.model.ResponseUser;
    import com.ecommerce.user.model.User;
    import com.ecommerce.user.model.enums.UserStatus;
    import com.ecommerce.user.repository.UserRepository;
    import lombok.extern.slf4j.Slf4j;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
    import org.springframework.jms.core.JmsTemplate;
    import org.springframework.stereotype.Service;

    import java.time.LocalDate;
    import java.util.Map;
    import java.util.UUID;

    @Service
    @Slf4j
    public class UserService {

        @Autowired
        UserRepository userRepository;
        @Autowired
        JmsTemplate jmsTemplate;
        @Autowired
        R2dbcEntityTemplate r2dbcEntityTemplate;

        public void  register(OrderData data) {
            User userData = new User();
            userData.setId(UUID.randomUUID().toString());
            userData.setName((String) ((Map<?, ?>) data.getData()).get("name"));
            userData.setUsername((String) ((Map<?, ?>) data.getData()).get("username"));
            userData.setPassword((String) ((Map<?, ?>) data.getData()).get("password"));
            userData.setBalance((Integer) ((Map<?, ?>) data.getData()).get("balance"));
            userData.setStatus(true);
            userData.setCreatedDate(LocalDate.now());
            log.info("user datanya :{}", userData);
            r2dbcEntityTemplate.insert(userData)
                .doOnSuccess(registered -> {
                    ResponseUser responseUser = new ResponseUser();
                    responseUser.setOrderId(data.getOrderId());
                    responseUser.setId(registered.getId());
                    responseUser.setName(registered.getName());
                    responseUser.setUsername(registered.getUsername());
                    responseUser.setPassword(registered.getPassword());
                    responseUser.setBalance(registered.getBalance());
                    responseUser.setStatus(registered.isStatus());
                    responseUser.setPriority(data.getPriority());
                    responseUser.setUserStatus("USER_APPROVED");
                    jmsTemplate.convertAndSend("queue.user.register.status", responseUser);
                    log.info("Message sent to orchestrator that user successfully registered: {}", responseUser);
                })
                .doOnError(error -> {
                    log.error("user register failed because : {}", error.getMessage());
                    ResponseUser responseUser = new ResponseUser();
                    responseUser.setOrderId(data.getOrderId());
                    responseUser.setId(userData.getId());
                    responseUser.setName(userData.getName());
                    responseUser.setUsername(userData.getUsername());
                    responseUser.setPassword(userData.getPassword());
                    responseUser.setBalance(userData.getBalance());
                    responseUser.setStatus(userData.isStatus());
                    responseUser.setPriority(data.getPriority());
                    responseUser.setUserStatus("USER_REJECTED");
                    jmsTemplate.convertAndSend("queue.user.register.status", responseUser);
                    log.info("message sent to orchestrator that user register rejected: {}", UserStatus.USER_REJECTED);
                }).subscribe();
        }

        public void rollbackRegister(OrderData data) {
            System.out.println((String) ((Map<?, ?>) data.getData()).get("id"));
            User userData = new User();
            userData.setId((String) ((Map<?, ?>) data.getData()).get("id"));
            userData.setName((String) ((Map<?, ?>) data.getData()).get("name"));
            userData.setUsername((String) ((Map<?, ?>) data.getData()).get("username"));
            userData.setPassword((String) ((Map<?, ?>) data.getData()).get("password"));
            userData.setBalance((Integer) ((Map<?, ?>) data.getData()).get("balance"));
            userData.setStatus(true);
            userData.setCreatedDate(LocalDate.now());
            System.out.println(userData.getId());
            userRepository.deleteById(userData.getId())
                .doOnSuccess(deleted -> {
                    ResponseUser responseUser = new ResponseUser();
                    responseUser.setOrderId(data.getOrderId());
                    responseUser.setId(null);
                    responseUser.setName(null);
                    responseUser.setUsername(null);
                    responseUser.setPassword(null);
                    responseUser.setBalance(null);
                    responseUser.setStatus(false);
                    responseUser.setPriority(data.getPriority());
                    responseUser.setUserStatus("ROLLBACKED");
                    log.info("User deleted, id : {}", userData.getId());
                    jmsTemplate.convertAndSend("queue.user.register.rollback.status", responseUser);
                })
                .doOnError(err -> {
                    log.error(err.getMessage());
                }).subscribe();
        }
    }
