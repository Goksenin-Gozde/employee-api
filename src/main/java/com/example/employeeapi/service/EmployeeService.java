package com.example.employeeapi.service;

import com.example.employeeapi.entity.Employee;
import com.example.employeeapi.entity.LeaveRequest;
import com.example.employeeapi.exception.EmployeeNotFoundException;
import com.example.employeeapi.helper.DateHelper;
import com.example.employeeapi.model.EmployeeDto;
import com.example.employeeapi.repository.EmployeeRepository;
import com.example.employeeapi.repository.LeaveRequestRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository, ModelMapper modelMapper, LeaveRequestRepository leaveRequestRepository) {
        this.employeeRepository = employeeRepository;
        this.modelMapper = modelMapper;
        this.leaveRequestRepository = leaveRequestRepository;
    }

    public Employee createEmployee(EmployeeDto employeeDto) {

        LocalDate hireDate = employeeDto.getHireDate();
        int remainingLeaveDays = DateHelper.calculateRemainingDaysForNewRecord(hireDate);

        Employee newEmployee = Employee.builder()
                .hireDate(hireDate)
                .name(employeeDto.getName())
                .remainingLeaveDays(remainingLeaveDays)
                .build();

        return employeeRepository.save(newEmployee);
    }

    private List<EmployeeDto> bulkUpdateEmployees(List<Employee> employees) {
        List<Employee> savedEmployees = employeeRepository.saveAll(employees);
        return employeeListToEmployeeDtoList(savedEmployees);
    }

    public Employee updateEmployee(Long id, EmployeeDto employee) {
        Employee existingEmployee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found"));
        if (Objects.nonNull(employee.getName())) {
            existingEmployee.setName(employee.getName());
        }
        if (Objects.nonNull(employee.getHireDate())) {
            existingEmployee.setHireDate(employee.getHireDate());
        }
        if (Objects.nonNull(employee.getRemainingLeaveDays())) {
            existingEmployee.setRemainingLeaveDays(employee.getRemainingLeaveDays());
        }
        return employeeRepository.save(existingEmployee);
    }

    public void deleteEmployee(Long id) {
        Employee existingEmployee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found"));
        List<LeaveRequest> leaveRequestList = leaveRequestRepository.findByEmployeeId(existingEmployee.getId());

        leaveRequestRepository.deleteAll(leaveRequestList);
        employeeRepository.delete(existingEmployee);
    }

    public List<Employee> getAllEmployees() {
        List<Employee> employees = employeeRepository.findAll();
        return employees;
    }

    public Employee getEmployeeById(Long id) {
        Optional<Employee> employee = employeeRepository.findById(id);
        if (!employee.isPresent()) {
            throw new EmployeeNotFoundException("Employee not found");
        }
        return employee.get();
    }

    @Scheduled(cron = "0 0 0 * * ?") // runs every night at midnight
    private void updateRemainingLeave() {
        List<Employee> employees = employeeRepository.findAll();
        List<Employee> employeesToUpdate = new ArrayList<>();
        for (Employee employee : employees) {
            LocalDate hireDate = employee.getHireDate();
            LocalDate currentDate = LocalDate.now();
            int yearsWorked = Period.between(hireDate, currentDate).getYears();
            if (yearsWorked >= 1 && yearsWorked <= 5) {
                employee.setRemainingLeaveDays(15);
            } else if (yearsWorked > 5 && yearsWorked <= 10) {
                employee.setRemainingLeaveDays(18);
            } else {
                employee.setRemainingLeaveDays(24);
            }
            employeesToUpdate.add(employee);
        }
        bulkUpdateEmployees(employeesToUpdate);
    }

    private List<EmployeeDto> employeeListToEmployeeDtoList(List<Employee> employees) {
        return employees.stream()
                .map(employee -> modelMapper.map(employee, EmployeeDto.class)).collect(Collectors.toList());

    }

    private List<Employee> employeeDtoListToEmployeeList(List<EmployeeDto> employeeDtos) {
        return employeeDtos.stream()
                .map(employeeDto -> modelMapper.map(employeeDto, Employee.class)).collect(Collectors.toList());
    }
}
