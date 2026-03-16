package com.project.practice.sap.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.project.practice.sap.model.enums.RoleType;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
@Table(name="roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name="roleType")
    private RoleType roleType;

    @ManyToMany(mappedBy = "roles")
    @JsonIgnore
    private List<User> users;



}
