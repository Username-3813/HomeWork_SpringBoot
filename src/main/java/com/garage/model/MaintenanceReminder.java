package com.garage.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "maintenance_reminders", indexes = {
    @Index(name = "idx_reminder_vehicle_id", columnList = "vehicle_id"),
    @Index(name = "idx_reminder_due_date", columnList = "due_date"),
    @Index(name = "idx_reminder_is_done", columnList = "is_done")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceReminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "estimated_cost")
    private Double estimatedCost;

    @Column(name = "is_done")
    private Boolean isDone = false;

    @Column(name = "reminder_days_before")
    private Integer reminderDaysBefore = 7;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}