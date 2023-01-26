package com.example.employeeapi.entity;

import com.example.employeeapi.enums.LeaveRequestReason;
import com.example.employeeapi.enums.LeaveRequestStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "leave_requests")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LeaveRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "total_leave_days", nullable = false)
    private Integer totalLeaveDays;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private LeaveRequestStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false)
    private LeaveRequestReason reason;

    @Column(name = "note", nullable = true)
    private String note;

}

