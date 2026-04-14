package com.project.practice.sap.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name="users")
@Data
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Integer id;

    @NotBlank(message = "Username must not be blank")
    @Column(name="username", unique = true, nullable = false)
    private String username;

    @NotBlank(message = "Email must not be blank")
    @Email(message = "Email must be a valid email address")
    @Column(name="email", unique = true, nullable = false)
    private String email;

    @JsonIgnore
    @Column(name="password_hash", nullable = false)
    private String passwordHash;

    @Transient  // forbids the password to interact with the DB directly (cannot be called from select nor saved as plain text)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) // allows us to use the setter when receiving the object from the controller but not the getter for a response
    @NotBlank(message = "Password must not be blank")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    @CreatedDate
    @Column(name="created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @JsonIgnore
    @ManyToMany
    @JoinTable(
            name="user_role",
            joinColumns = @JoinColumn(name="user_id"),
            inverseJoinColumns = @JoinColumn(name="role_id")
    )
    private List<Role> roles;

    @JsonIgnore
    @OneToMany(mappedBy = "createdBy")
    @ToString.Exclude // lombok issue to be addressed later
    private List<Document> documents;
}
