package com.garage.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ProfileDto {
    private String email;
    private String lastName;
    private String firstName;
    private String patronymic;
    private LocalDate birthDate;
    private String phone;
    private String profilePhoto;
}