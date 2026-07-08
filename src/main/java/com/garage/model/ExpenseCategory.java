package com.garage.model;

public enum ExpenseCategory {
    FUEL("Топливо"),
    REPAIR("Ремонт"),
    INSURANCE("Страховка"),
    OTHER("Прочее"),
    FINE("Штраф"),
    WASH("Мойка");

    private final String displayName;

    ExpenseCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static String getDisplayName(String code) {
        if (code == null) return null;
        try {
            return ExpenseCategory.valueOf(code).getDisplayName();
        } catch (IllegalArgumentException e) {
            return code; // если не найдено, возвращаем как есть
        }
    }
}