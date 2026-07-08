package com.garage.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "site_content")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SiteContent {

    @Id
    private String page; // "about" или "contacts"

    @Column(columnDefinition = "TEXT")
    private String content; // HTML-текст

    @Column(name = "updated_at")
    private java.time.LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = java.time.LocalDateTime.now();
    }
}