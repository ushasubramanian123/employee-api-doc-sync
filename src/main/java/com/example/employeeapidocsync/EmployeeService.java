package com.example.employeeapidocsync;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

@Service
public class EmployeeService {

    private final Map<Long, Employee> employees = new LinkedHashMap<>();
    private long nextId = 1;

    public synchronized List<Employee> findAll(String department) {
        if (department == null) {
            return List.copyOf(employees.values());
        }
        String trimmed = department.trim();
        if (trimmed.isEmpty()) {
            return List.of();
        }
        return employees.values().stream()
                .filter(e -> e.department() != null && e.department().equalsIgnoreCase(trimmed))
                .toList();
    }

    public synchronized Optional<Employee> findById(Long id) {
        return Optional.ofNullable(employees.get(id));
    }

    public synchronized Employee create(CreateEmployeeRequest request) {
        Employee employee = new Employee(nextId++, request.name(), request.email(), request.department());
        employees.put(employee.id(), employee);
        return employee;
    }
}