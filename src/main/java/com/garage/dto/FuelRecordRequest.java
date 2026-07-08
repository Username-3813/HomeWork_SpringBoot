package com.garage.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import java.time.LocalDate;

@Data
public class FuelRecordRequest {
    @NotNull(message = "Дата заправки обязательна")
    private LocalDate date;

    @NotNull(message = "Пробег обязателен")
    @PositiveOrZero(message = "Пробег не может быть отрицательным")
    private Integer odometer;

    @NotNull(message = "Количество литров обязательно")
    @Positive(message = "Литров должно быть больше нуля")
    private Double liters;

    @NotNull(message = "Цена за литр обязательна")
    @Positive(message = "Цена должна быть больше нуля")
    private Double pricePerLiter;

    private Boolean isFullTank;
}