package com.example.employeeapi.model;

import lombok.*;

import java.time.LocalDate;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeDto {
    private String name;
    private LocalDate hireDate;
    private Integer remainingLeaveDays;
}
