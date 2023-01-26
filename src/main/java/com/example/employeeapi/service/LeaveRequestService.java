package com.example.employeeapi.service;

import com.example.employeeapi.entity.Employee;
import com.example.employeeapi.entity.LeaveRequest;
import com.example.employeeapi.enums.LeaveRequestStatus;
import com.example.employeeapi.exception.EmployeeNotFoundException;
import com.example.employeeapi.exception.InvalidLeaveRequestException;
import com.example.employeeapi.exception.LeaveRequestNotFoundException;
import com.example.employeeapi.helper.DateHelper;
import com.example.employeeapi.model.LeaveRequestDto;
import com.example.employeeapi.repository.EmployeeRepository;
import com.example.employeeapi.repository.LeaveRequestRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

@Service
public class LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeRepository employeeRepository;
    private final ModelMapper modelMapper;


    @Autowired
    public LeaveRequestService(LeaveRequestRepository leaveRequestRepository, EmployeeRepository employeeRepository, ModelMapper modelMapper) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.employeeRepository = employeeRepository;
        this.modelMapper = modelMapper;
    }

    public List<LeaveRequest> list() {
        return leaveRequestRepository.findAll();
    }

    public LeaveRequest createLeaveRequest(LeaveRequestDto leaveRequestDto) {
        Employee employee = employeeRepository.findById(leaveRequestDto.getEmployeeId())
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found"));

        if (Objects.isNull(leaveRequestDto.getTotalLeaveDays())) {
            int totalLeaveDays = DateHelper.getDaysBetween(leaveRequestDto.getStartDate(), leaveRequestDto.getEndDate());
            leaveRequestDto.setTotalLeaveDays(totalLeaveDays);
        }

        LocalDate today = LocalDate.now();
        LocalDate hireDate = employee.getHireDate();
        if (ChronoUnit.YEARS.between(hireDate, today) == 0 && leaveRequestDto.getTotalLeaveDays() > 5) {
            throw new InvalidLeaveRequestException("Newly hired employees can only take 5 days of leave in advance.");
        }
        // This validation can be removed due to business needs. But keeping it and directing employees to request
        // leaving days on time is a better practice
        if (leaveRequestDto.getStartDate().isBefore(today)) {
            throw new InvalidLeaveRequestException("Cannot create a leave request for a past date.");
        }
        if (leaveRequestDto.getTotalLeaveDays() > employee.getRemainingLeaveDays()) {
            throw new InvalidLeaveRequestException("Cannot create a leave request exceeding the remaining leave days of the employee.");
        }
        if (DateHelper.isWeekend(leaveRequestDto.getStartDate()) && DateHelper.isWeekend(leaveRequestDto.getEndDate())) {
            throw new InvalidLeaveRequestException("Leave request should contain at least one weekend day.");
        }

        LeaveRequest leaveRequest = modelMapper.map(leaveRequestDto, LeaveRequest.class);
        leaveRequest.setStatus(LeaveRequestStatus.WAITING_FOR_APPROVAL);

        return leaveRequestRepository.save(leaveRequest);
    }


    public LeaveRequest updateLeaveRequest(Long id, LeaveRequestDto leaveRequestDto) {
        LeaveRequest existingLeaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new LeaveRequestNotFoundException("Leave request not found"));

        if (Objects.nonNull(leaveRequestDto.getReason())) {
            existingLeaveRequest.setReason(leaveRequestDto.getReason());
        }

        LocalDate startDate = Objects.isNull(leaveRequestDto.getStartDate()) ? existingLeaveRequest.getStartDate()
                : leaveRequestDto.getStartDate();
        LocalDate endDate = Objects.isNull(leaveRequestDto.getEndDate()) ? existingLeaveRequest.getEndDate()
                : leaveRequestDto.getEndDate();

        if (!startDate.isEqual(existingLeaveRequest.getStartDate())
                || !endDate.isEqual(existingLeaveRequest.getEndDate())) {

            existingLeaveRequest.setStartDate(startDate);
            existingLeaveRequest.setEndDate(endDate);

            int totalLeaveDays = DateHelper.getDaysBetween(startDate, endDate);
            existingLeaveRequest.setTotalLeaveDays(totalLeaveDays);
        }

        return leaveRequestRepository.save(existingLeaveRequest);
    }

    public LeaveRequest updateLeaveRequestStatus(Long id, LeaveRequestStatus status) {
        LeaveRequest existingLeaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new LeaveRequestNotFoundException("Leave request not found"));

        if(existingLeaveRequest.getStatus().equals(LeaveRequestStatus.APPROVED)){
            throw new InvalidLeaveRequestException("You can not update APPROVED leave requests.");
        }
        Employee employee = existingLeaveRequest.getEmployee();
        if (status == LeaveRequestStatus.APPROVED) {
            LocalDate today = LocalDate.now();
            LocalDate hireDate = employee.getHireDate();
            if (DateHelper.getDaysBetween(hireDate, today) == 0 && existingLeaveRequest.getTotalLeaveDays() > 5) {
                throw new InvalidLeaveRequestException("Newly hired employees can only take 5 days of leave in advance.");
            }
            // This validation can be removed due to business needs. But keeping it and directing managers to approve or
            // reject leave requests on time is a better practice
            if (existingLeaveRequest.getStartDate().isBefore(today)) {
                throw new InvalidLeaveRequestException("Cannot approve a leave request for a past date.");
            }
            if (existingLeaveRequest.getTotalLeaveDays() > employee.getRemainingLeaveDays()) {
                throw new InvalidLeaveRequestException("Cannot approve a leave request exceeding the remaining leave days of the employee.");
            }
            int leaveDays = DateHelper.calculateLeaveDays(existingLeaveRequest.getStartDate(), existingLeaveRequest.getEndDate());
            employee.setRemainingLeaveDays(employee.getRemainingLeaveDays() - leaveDays);
            employeeRepository.save(employee);
        }
        existingLeaveRequest.setStatus(status);
        return leaveRequestRepository.save(existingLeaveRequest);
    }

    public void deleteLeaveRequest(Long id) {
        LeaveRequest existingLeaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new LeaveRequestNotFoundException("Leave request not found"));

        leaveRequestRepository.delete(existingLeaveRequest);
    }


}

