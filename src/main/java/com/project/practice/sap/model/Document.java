package com.project.practice.sap.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name="documents")
@EntityListeners(AuditingEntityListener.class)
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name="name", unique = true, nullable = false)
    private String name;

    @CreatedDate
    @Column(name="created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    // done so documents are not deleted upon user deletion (simply make it false if we want to delete all documemtns linked to a user)
    @JoinColumn(name = "user_id", nullable = true)
    @ToString.Exclude // infinite loop issue with lombok
    private User createdBy;
}
