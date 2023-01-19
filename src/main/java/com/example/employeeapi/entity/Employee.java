package com.example.employeeapi.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "employees")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;

    @Column(name = "remaining_leave_days", nullable = false)
    private Integer remainingLeaveDays;

}

