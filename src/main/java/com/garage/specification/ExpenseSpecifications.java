package com.garage.specification;

import com.garage.model.Expense;
import com.garage.model.Vehicle;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Join;
import java.time.LocalDate;
import java.util.List;

public class ExpenseSpecifications {

    public static Specification<Expense> filterByUserId(Long userId) {
        return (root, query, cb) -> {
            Join<Expense, Vehicle> vehicleJoin = root.join("vehicle");
            return cb.equal(vehicleJoin.get("user").get("id"), userId);
        };
    }

    public static Specification<Expense> filterByVehicleId(Long vehicleId) {
        return (root, query, cb) -> cb.equal(root.get("vehicle").get("id"), vehicleId);
    }

    public static Specification<Expense> filterByDateBetween(LocalDate from, LocalDate to) {
        return (root, query, cb) -> {
            if (from == null && to == null) return cb.conjunction();
            if (from == null) return cb.lessThanOrEqualTo(root.get("date"), to);
            if (to == null) return cb.greaterThanOrEqualTo(root.get("date"), from);
            return cb.between(root.get("date"), from, to);
        };
    }

    public static Specification<Expense> filterByCategories(List<String> categories) {
        return (root, query, cb) -> {
            if (categories == null || categories.isEmpty()) return cb.conjunction();
            return root.get("category").in(categories);
        };
    }

    public static Specification<Expense> filterByVehicleTypes(List<String> vehicleTypes) {
        return (root, query, cb) -> {
            if (vehicleTypes == null || vehicleTypes.isEmpty()) return cb.conjunction();
            Join<Expense, Vehicle> vehicleJoin = root.join("vehicle");
            return vehicleJoin.get("type").in(vehicleTypes);
        };
    }
}