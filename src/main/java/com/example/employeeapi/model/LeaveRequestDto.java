package com.example.employeeapi.model;

import com.example.employeeapi.enums.LeaveRequestReason;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveRequestDto {
    private Long employeeId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer totalLeaveDays;
    private LeaveRequestReason reason;
}

