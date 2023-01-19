package com.example.employeeapi;

import com.example.employeeapi.entity.Employee;
import com.example.employeeapi.exception.EmployeeNotFoundException;
import com.example.employeeapi.model.EmployeeDto;
import com.example.employeeapi.repository.EmployeeRepository;
import com.example.employeeapi.service.EmployeeService;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class EmployeeServiceTest {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private ModelMapper modelMapper;

    @MockBean
    private EmployeeRepository employeeRepository;

    @Test
    public void testCreateEmployee() {
        // given
        LocalDate hireDate = LocalDate.of(2020, 1, 1);
        EmployeeDto employeeDto = EmployeeDto.builder()
                .name("John Doe")
                .hireDate(hireDate)
                .build();
        Employee savedEmployee = Employee.builder()
                .id(1L)
                .name("John Doe")
                .hireDate(hireDate)
                .remainingLeaveDays(15)
                .build();
        doReturn(savedEmployee).when(employeeRepository).save(any(Employee.class));

        // when
        Employee result = employeeService.createEmployee(employeeDto);

        // then
        assertThat(result.getName()).isEqualTo("John Doe");
        assertThat(result.getHireDate()).isEqualTo(hireDate);
        assertThat(result.getRemainingLeaveDays()).isEqualTo(15);
    }

    @Test
    public void testUpdateEmployee() {
        // given
        Employee employee = Employee.builder()
                .id(1L)
                .name("John Doe")
                .hireDate(LocalDate.of(2020, 1, 1))
                .remainingLeaveDays(15)
                .build();
        doReturn(Optional.of(employee)).when(employeeRepository).findById(eq(1L));
        doReturn(employee).when(employeeRepository).save(eq(employee));

        // when
        Employee result = employeeService.updateEmployee(1L, employee);

        // then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("John Doe");
        assertThat(result.getHireDate()).isEqualTo(LocalDate.of(2020, 1, 1));
    }

    @Test
    public void testGetAllEmployees() {
        // Given
        List<Employee> employees = Arrays.asList(
                new Employee(1L, "John Doe", LocalDate.now(), 15),
                new Employee(2L, "Jane Smith", LocalDate.now(), 18)
        );
        when(employeeRepository.findAll()).thenReturn(employees);

        // When
        List<EmployeeDto> employeeDtos = employeeService.getAllEmployees();

        // Then
        assertEquals(2, employeeDtos.size());
        assertEquals("John Doe", employeeDtos.get(0).getName());
        assertEquals("Jane Smith", employeeDtos.get(1).getName());
    }

    @Test
    public void testGetEmployeeById() {
        // Given
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(new Employee(1L, "John Doe", LocalDate.now(), 15)));

        // When
        EmployeeDto employeeDto = employeeService.getEmployeeById(1L);

        // Then
        assertEquals("John Doe", employeeDto.getName());
    }

    @Test
    public void testGetEmployeeById_EmployeeNotFound() {
        // Given
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(EmployeeNotFoundException.class, () -> employeeService.getEmployeeById(1L));
    }

    @Test
    public void whenUpdateEmployee_thenReturnUpdatedEmployee() {
        // Arrange
        EmployeeDto employeeDto = new EmployeeDto("John Doe", LocalDate.now(), 0);
        Employee createdEmployee = employeeService.createEmployee(employeeDto);

        EmployeeDto updatedEmployeeDto = new EmployeeDto("Jane Doe", LocalDate.now(), 0);

        // Act
        Employee updatedEmployee = employeeService.updateEmployee(createdEmployee.getId(), modelMapper.map(updatedEmployeeDto, Employee.class));

        // Assert
        assertThat(updatedEmployee.getName()).isEqualTo(updatedEmployeeDto.getName());
        assertThat(updatedEmployee.getHireDate()).isEqualTo(updatedEmployeeDto.getHireDate());
        assertThat(updatedEmployee.getRemainingLeaveDays()).isEqualTo(createdEmployee.getRemainingLeaveDays());
    }

    @Test(expected = EmployeeNotFoundException.class)
    public void whenDeleteEmployee_thenEmployeeNotFound() {
        // Arrange
        EmployeeDto employeeDto = new EmployeeDto("John Doe", LocalDate.now(), 0);
        Employee createdEmployee = employeeService.createEmployee(employeeDto);

        // Act
        employeeService.deleteEmployee(createdEmployee.getId());
        employeeService.getEmployeeById(createdEmployee.getId());

        // Assert
        // Exception is expected
    }
}