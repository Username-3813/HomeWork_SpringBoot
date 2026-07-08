package com.garage.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationDto {
    private Long id;
    private String type;      // "MAINTENANCE" или "INSURANCE"
    private String title;
    private String description;
    private LocalDate date;
    private Long vehicleId;
    private String vehicleName;
    private String vehicleType;
    private String brand;      // марка ТС
    private String model;      // модель ТС
    private Double cost;
    private Boolean isDone;
}