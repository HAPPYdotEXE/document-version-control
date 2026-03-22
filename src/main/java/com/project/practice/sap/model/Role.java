// package com.project.practice.sap.model;


// import com.fasterxml.jackson.annotation.JsonIgnore;
// import com.project.practice.sap.model.enums.RoleType;
// import jakarta.persistence.*;
// import lombok.Data;

// import java.util.List;

// @Entity
// @Data
// @Table(name="roles")
// public class Role {

//     @Id
//     @GeneratedValue(strategy = GenerationType.IDENTITY)
//     private Integer id;

//     @Enumerated(EnumType.STRING)
//     @Column(name="roleType")
//     private RoleType roleType;

//     @ManyToMany(mappedBy = "roles")
//     @JsonIgnore
//     private List<User> users;



// }



// package com.project.practice.sap.model;

// import jakarta.persistence.*;

// @Entity
// @Table(name = "roles")
// public class Role {

//     @Id
//     @GeneratedValue(strategy = GenerationType.IDENTITY)
//     private Long id;

//     @Column(nullable = false, unique = true)
//     private String name;

//     public Long getId() {
//         return id;
//     }

//     public String getName() {
//         return name;
//     }

//     public void setId(Long id) {
//         this.id = id;
//     }

//     public void setName(String name) {
//         this.name = name;
//     }
// }


package com.project.practice.sap.model;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.project.practice.sap.model.enums.RoleType;

import jakarta.persistence.*;

@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private RoleType roleType;

    @JsonIgnore
    @ManyToMany(mappedBy = "roles")
    private Set<User> users = new HashSet<>();

    public Long getId() {
        return id;
    }

    public RoleType getRoleType() {
        return roleType;
    }

    public Set<User> getUsers() {
        return users;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setRoleType(RoleType roleType) {
        this.roleType = roleType;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }
}