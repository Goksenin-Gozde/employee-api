package com.example.employeeapi;

import com.example.employeeapi.entity.Employee;
import com.example.employeeapi.exception.EmployeeNotFoundException;
import com.example.employeeapi.model.EmployeeDto;
import com.example.employeeapi.repository.EmployeeRepository;
import com.example.employeeapi.service.EmployeeService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class EmployeeServiceTest {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private ModelMapper modelMapper;

    @MockBean
    private EmployeeRepository employeeRepository;

    private Employee employee;
    private EmployeeDto employeeDto;


    @Before
    public void setUp() {
        employee = new Employee();
        employee.setId(1L);
        employee.setName("John Doe");
        employee.setHireDate(LocalDate.of(2020, 1, 1));
        employee.setRemainingLeaveDays(20);

        employeeDto = new EmployeeDto("John Doe2", LocalDate.now(), 0);
    }


    @Test
    public void testCreateEmployee() {
        LocalDate hireDate = LocalDate.of(2020, 1, 1);

        doReturn(employee).when(employeeRepository).save(any(Employee.class));

        Employee result = employeeService.createEmployee(employeeDto);

        assertEquals(result.getName(), "John Doe");
        assertEquals(result.getHireDate(), hireDate);
        assertEquals(Optional.ofNullable(result.getRemainingLeaveDays()), Optional.of(20));
    }

    @Test
    public void testUpdateEmployee() {

        EmployeeDto updatedEmployee = EmployeeDto.builder()
                .name("Jane Doe")
                .hireDate(LocalDate.of(2020, 1, 1))
                .remainingLeaveDays(15)
                .build();

        doReturn(Optional.of(employee)).when(employeeRepository).findById(eq(1L));
        doReturn(employee).when(employeeRepository).save(eq(employee));

        Employee result = employeeService.updateEmployee(1L, updatedEmployee);

        assertEquals(Optional.ofNullable(result.getId()), Optional.of(1L));
        assertEquals(result.getName(), "Jane Doe");
        assertEquals(result.getHireDate(), LocalDate.of(2020, 1, 1));
    }

    @Test
    public void testGetAllEmployees() {
        List<Employee> employees = Arrays.asList(
                new Employee(1L, "John Doe", LocalDate.now(), 15),
                new Employee(2L, "Jane Smith", LocalDate.now(), 18)
        );
        when(employeeRepository.findAll()).thenReturn(employees);

        List<Employee> employeeDtos = employeeService.getAllEmployees();

        assertEquals(2, employeeDtos.size());
        assertEquals("John Doe", employeeDtos.get(0).getName());
        assertEquals("Jane Smith", employeeDtos.get(1).getName());
    }

    @Test
    public void testGetEmployeeById() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(new Employee(1L, "John Doe", LocalDate.now(), 15)));

        Employee employeeDto = employeeService.getEmployeeById(1L);

        assertEquals("John Doe", employeeDto.getName());
    }

    @Test
    public void testGetEmployeeById_EmployeeNotFound() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EmployeeNotFoundException.class, () -> employeeService.getEmployeeById(1L));
    }

    @Test
    public void whenUpdateEmployee_thenReturnUpdatedEmployee() {
        when(employeeService.createEmployee(employeeDto)).thenReturn(employee);
        doReturn(employee).when(employeeRepository).save(any(Employee.class));
        Employee createdEmployee = employeeService.createEmployee(employeeDto);

        EmployeeDto updatedEmployeeDto = new EmployeeDto("Jane Doe", LocalDate.now(), 0);
        when(employeeRepository.findById(createdEmployee.getId())).thenReturn(Optional.of(createdEmployee));

        Employee updatedEmployee = employeeService.updateEmployee(createdEmployee.getId(), modelMapper.map(updatedEmployeeDto, EmployeeDto.class));

        assertEquals(updatedEmployee.getName(), updatedEmployeeDto.getName());
        assertEquals(updatedEmployee.getHireDate(), updatedEmployeeDto.getHireDate());
        assertEquals(updatedEmployee.getRemainingLeaveDays(), createdEmployee.getRemainingLeaveDays());
    }

    @Test(expected = EmployeeNotFoundException.class)
    public void whenDeleteEmployee_thenEmployeeNotFound() {

        doReturn(employee).when(employeeRepository).save(any(Employee.class));
        Employee createdEmployee = employeeService.createEmployee(employeeDto);
        when(employeeRepository.findById(createdEmployee.getId())).thenReturn(Optional.ofNullable(null));

        employeeService.deleteEmployee(createdEmployee.getId());
        employeeService.getEmployeeById(createdEmployee.getId());

    }
}