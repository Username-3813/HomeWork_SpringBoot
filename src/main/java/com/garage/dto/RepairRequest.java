package com.garage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import java.time.LocalDate;

@Data
public class RepairRequest {
    @NotNull(message = "Дата ремонта обязательна")
    private LocalDate date;

    @NotNull(message = "Пробег обязателен")
    @PositiveOrZero(message = "Пробег не может быть отрицательным")
    private Integer odometer;

    @NotBlank(message = "Описание ремонта обязательно")
    private String description;

    @NotNull(message = "Стоимость обязательна")
    @Positive(message = "Стоимость должна быть больше нуля")
    private Double cost;

    private String workshop;
}