package com.garage.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import java.time.LocalDate;

@Data
public class ReminderRequest {
    private String title; // заполняется автоматически из reminderType

    private String description;

    @NotNull(message = "Дата выполнения обязательна")
    @FutureOrPresent(message = "Дата не может быть в прошлом")
    private LocalDate dueDate;

    @PositiveOrZero(message = "Стоимость не может быть отрицательной")
    private Double estimatedCost;

    private Boolean isDone;

    @Positive(message = "Количество дней должно быть больше нуля")
    private Integer reminderDaysBefore;

    private String reminderType; // "Техническое обслуживание", "Замена масла", "Плановая запись в автосервис", "Другое"
    private String customTitle;  // если выбрано "Другое"
}