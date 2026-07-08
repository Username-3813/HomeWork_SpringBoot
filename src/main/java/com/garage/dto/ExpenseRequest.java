package com.garage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ExpenseRequest {
    @NotBlank(message = "Категория обязательна")
    private String category;

    @NotNull(message = "Дата обязательна")
    private LocalDate date;

    @NotNull(message = "Сумма обязательна")
    @Positive(message = "Сумма должна быть больше нуля")
    private Double amount;

    private String description;
}