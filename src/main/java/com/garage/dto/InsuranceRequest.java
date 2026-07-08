package com.garage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Data
public class InsuranceRequest {
    @NotBlank(message = "Номер полиса обязателен")
    private String policyNumber;

    private String company;

    @NotBlank(message = "Тип страховки обязателен")
    private String type;

    @NotNull(message = "Дата начала обязательна")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @NotNull(message = "Стоимость обязательна")
    @Positive(message = "Стоимость должна быть больше нуля")
    private Double cost;

    private Integer periodMonths;

    private MultipartFile frontFile;
    private MultipartFile backFile;
}