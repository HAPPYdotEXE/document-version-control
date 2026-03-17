package com.project.practice.sap.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name="users")
@Data
public class User {

    @Id //all tables in DB need a primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY) //auto generates id
    @Column(name="id")
    private Integer id;

    @Column(name="username")
    private String username;

    @Column(name="email", unique = true, nullable = false)
    private String email;

    @Column(name="passwordHash")
    private String passwordHash;

    @CreatedDate
    @Column(name="createdAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @JsonIgnore
    @ManyToMany
    @JoinTable(
            name="user_role",
            joinColumns = @JoinColumn(name="user_id"),
            inverseJoinColumns = @JoinColumn(name="role_id")
    )    private List<Role> roles;

}
