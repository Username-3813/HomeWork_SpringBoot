package com.garage.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CategoryOption {
    private String code;
    private String displayName;
}