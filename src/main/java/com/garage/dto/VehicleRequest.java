package com.garage.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class VehicleRequest {
    private String name;
    private String type;
    private String brand;
    private String model;
    private Integer year;
    private String vin;
    private String licensePlate;
    private Double initialOdometer;
    private String odometerUnit = "km";
    private String photoUrl;

    // Фото ТС (загружаемый файл)
    private MultipartFile photoFile;
}