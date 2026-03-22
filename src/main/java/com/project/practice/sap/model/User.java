// package com.project.practice.sap.model;

// import com.fasterxml.jackson.annotation.JsonIgnore;
// import jakarta.persistence.*;
// import lombok.Data;
// import org.springframework.data.annotation.CreatedDate;

// import java.time.LocalDateTime;
// import java.util.List;

// @Entity
// @Table(name="users")
// @Data
// public class User {

//     @Id //all tables in DB need a primary key
//     @GeneratedValue(strategy = GenerationType.IDENTITY) //auto generates id
//     @Column(name="id")
//     private Long id;

//     @Column(name="username")
//     private String username;

//     @Column(name="email", unique = true, nullable = false)
//     private String email;

//     @Column(name="passwordHash")
//     private String passwordHash;

//     @CreatedDate
//     @Column(name="createdAt", nullable = false, updatable = false)
//     private LocalDateTime createdAt;

//     @JsonIgnore
//     @ManyToMany
//     @JoinTable(
//             name="user_role",
//             joinColumns = @JoinColumn(name="user_id"),
//             inverseJoinColumns = @JoinColumn(name="role_id")
//     )    private List<Role> roles;

//     public Long getId() {
//         return id;
//     }
    
//     public void setId(Long id) {
//         this.id = id;
//     }
//     public String getUsername() {
//         return username;
//     }
//     public void setUsername(String username) {
//         this.username = username;
//     }
//     public String getEmail() {
//         return email;
//     }
//     public void setEmail(String email) {
//         this.email = email;
//     }
//     public String getPasswordHash() {
//         return passwordHash;
//     }
//     public void setPasswordHash(String passwordHash) {
//         this.passwordHash = passwordHash;
//     }
//     public LocalDateTime getCreatedAt() {
//         return createdAt;
//     }
//     public void setCreatedAt(LocalDateTime createdAt) {
//         this.createdAt = createdAt;
//     }
// }



//package com.project.practice.sap.model;

// import java.util.HashSet;
// import java.util.Set;

// import jakarta.persistence.*;

// @Entity
// @Table(name = "users")
// public class User {

//     @Id
//     @GeneratedValue(strategy = GenerationType.IDENTITY)
//     private Long id;

//     @Column(nullable = false, unique = true)
//     private String username;

//     @Column(nullable = false, unique = true)
//     private String email;

//     @Column(nullable = false)
//     private String passwordHash;

//     // @ManyToMany
//     // @JoinTable(
//     //     name = "user_roles",
//     //     joinColumns = @JoinColumn(name = "user_id"),
//     //     inverseJoinColumns = @JoinColumn(name = "role_id")
//     // )
//     // private Set<Role> roles = new HashSet<>();

//     public Long getId() {
//         return id;
//     }

//     public String getUsername() {
//         return username;
//     }

//     public String getEmail() {
//         return email;
//     }

//     public String getPasswordHash() {
//         return passwordHash;
//     }

//     // public Set<Role> getRoles() {
//     //     return roles;
//     // }

//     public void setId(Long id) {
//         this.id = id;
//     }

//     public void setUsername(String username) {
//         this.username = username;
//     }

//     public void setEmail(String email) {
//         this.email = email;
//     }

//     public void setPasswordHash(String passwordHash) {
//         this.passwordHash = passwordHash;
//     }

//     // public void setRoles(Set<Role> roles) {
//     //     this.roles = roles;
//     // }
// }

package com.project.practice.sap.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import com.project.practice.sap.model.enums.RoleType;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public boolean hasRole(RoleType roleType) {
        return roles.stream().anyMatch(role -> role.getRoleType() == roleType);
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }
}