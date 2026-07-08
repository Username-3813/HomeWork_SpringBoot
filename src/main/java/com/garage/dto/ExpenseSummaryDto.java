package com.garage.dto;

import lombok.Data;
import java.util.List;

@Data
public class ExpenseSummaryDto {
    private double totalExpenses;
    private double averageAmount;
    private int count;
    private List<CategoryExpenseDto> categoryBreakdown;
}