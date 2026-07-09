package com.garage.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "vehicles", indexes = {
    @Index(name = "idx_vehicle_user_id", columnList = "user_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 30)
    private String type;

    @Column(length = 50)
    private String brand;

    @Column(length = 50)
    private String model;

    private Integer year;

    @Column(length = 17, unique = true)
    private String vin;

    @Column(name = "license_plate", length = 20)
    private String licensePlate;

    @Column(name = "initial_odometer")
    private Double initialOdometer;

    @Column(name = "odometer_unit", length = 10)
    private String odometerUnit = "km"; // "km" или "mh"

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MaintenanceReminder> reminders = new ArrayList<>();


    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InsurancePolicy> insurances = new ArrayList<>();

    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Document> documents = new ArrayList<>();

    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Expense> expenses = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (odometerUnit == null) odometerUnit = "km";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}