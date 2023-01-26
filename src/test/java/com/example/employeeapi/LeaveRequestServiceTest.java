package com.example.employeeapi;

import com.example.employeeapi.entity.Employee;
import com.example.employeeapi.entity.LeaveRequest;
import com.example.employeeapi.exception.EmployeeNotFoundException;
import com.example.employeeapi.exception.InvalidLeaveRequestException;
import com.example.employeeapi.model.LeaveRequestDto;
import com.example.employeeapi.repository.EmployeeRepository;
import com.example.employeeapi.repository.LeaveRequestRepository;
import com.example.employeeapi.service.LeaveRequestService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.modelmapper.ModelMapper;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LeaveRequestServiceTest {

    @InjectMocks
    private LeaveRequestService leaveRequestService;

    @Mock
    private LeaveRequestRepository leaveRequestRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private ModelMapper modelMapper;

    private Employee employee;
    private LeaveRequestDto leaveRequestDto;
    private LeaveRequest leaveRequest;

    @Before
    public void setUp() {
        employee = new Employee();
        employee.setId(1L);
        employee.setHireDate(LocalDate.of(2020, 1, 1));
        employee.setRemainingLeaveDays(20);

        leaveRequestDto = new LeaveRequestDto();
        leaveRequestDto.setEmployeeId(employee.getId());
        leaveRequestDto.setStartDate(LocalDate.of(2023, 2, 3));
        leaveRequestDto.setEndDate(LocalDate.of(2023, 2, 5));
        leaveRequestDto.setTotalLeaveDays(5);

        leaveRequest = new LeaveRequest();
        leaveRequest.setEmployee(employee);
        leaveRequest.setStartDate(LocalDate.of(2023, 2, 3));
        leaveRequest.setEndDate(LocalDate.of(2023, 2, 5));
        leaveRequest.setTotalLeaveDays(5);

        LeaveRequest existingLeaveRequest = new LeaveRequest();
        existingLeaveRequest.setEmployee(employee);
        existingLeaveRequest.setStartDate(LocalDate.of(2023, 2, 3));
        existingLeaveRequest.setEndDate(LocalDate.of(2023, 2, 5));
        existingLeaveRequest.setTotalLeaveDays(5);
    }

    @Test
    public void testCreateLeaveRequest_valid() {
        when(employeeRepository.findById(employee.getId())).thenReturn(Optional.of(employee));
        when(modelMapper.map(leaveRequestDto, LeaveRequest.class)).thenReturn(leaveRequest);
        when(leaveRequestRepository.save(leaveRequest)).thenReturn(leaveRequest);

        LeaveRequest result = leaveRequestService.createLeaveRequest(leaveRequestDto);

        assertNotNull(result);
        assertEquals(leaveRequest, result);
    }

    @Test(expected = EmployeeNotFoundException.class)
    public void testCreateLeaveRequest_employeeNotFound() {
        when(employeeRepository.findById(employee.getId())).thenReturn(Optional.empty());

        leaveRequestService.createLeaveRequest(leaveRequestDto);
    }

    @Test(expected = InvalidLeaveRequestException.class)
    public void testCreateLeaveRequest_startDateInPast() {
        leaveRequestDto.setStartDate(LocalDate.of(2021, 12, 31));
        when(employeeRepository.findById(leaveRequest.getEmployee().getId())).thenReturn(Optional.ofNullable(employee));

        leaveRequestService.createLeaveRequest(leaveRequestDto);
    }

    @Test(expected = InvalidLeaveRequestException.class)
    public void testCreateLeaveRequest_exceedingLeaveRequest() {
        leaveRequestDto.setStartDate(LocalDate.of(2021, 12, 31));
        leaveRequestDto.setEndDate(LocalDate.of(2025, 12, 31));
        when(employeeRepository.findById(leaveRequest.getEmployee().getId())).thenReturn(Optional.ofNullable(employee));

        leaveRequestService.createLeaveRequest(leaveRequestDto);
    }
}

