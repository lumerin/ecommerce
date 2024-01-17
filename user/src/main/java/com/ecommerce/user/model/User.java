package com.ecommerce.user.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;

@Table("users")
@Data
public class User {
    @Id
    String id;
    String name;
    String username;
    String password;
    Integer balance;
    boolean status;
    @Column("created_date")
    LocalDate createdDate;
    @Column("updated_date")
    LocalDate updatedDate;
}

