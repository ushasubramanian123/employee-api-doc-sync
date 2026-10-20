package com.example.employeeapidocsync;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.annotation.DirtiesContext;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class EmployeeApiDocSyncApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getAllReturnsAnEmptyJsonArray() throws Exception {
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getByIdReturnsNotFoundForUnknownId() throws Exception {
        mockMvc.perform(get("/api/employees/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Employee not found"));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void postCreatesEmployeesWithIncrementingIdsAndGetReturnsThem() throws Exception {
        mockMvc.perform(post("/api/employees")
                        .contentType(APPLICATION_JSON)
                        .content("{\"name\":\"John Doe\",\"email\":\"john@example.com\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));

        mockMvc.perform(post("/api/employees")
                        .contentType(APPLICATION_JSON)
                        .content("{\"name\":\"Jane Doe\",\"email\":\"jane@example.com\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2));

        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("John Doe"))
            .andExpect(jsonPath("$.email").value("john@example.com"));

        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].name").value("John Doe"))
            .andExpect(jsonPath("$[0].email").value("john@example.com"));
    }

    @Test
    void postRejectsBlankNameInvalidEmailAndMissingFields() throws Exception {
        assertInvalidEmployee("{\"name\":\" \",\"email\":\"john@example.com\"}");
        assertInvalidEmployee("{\"name\":\"John Doe\",\"email\":\"not-an-email\"}");
        assertInvalidEmployee("{\"email\":\"john@example.com\"}");
        assertInvalidEmployee("{\"name\":\"John Doe\"}");
    }

    @Test
    void postRejectsMalformedJson() throws Exception {
        assertInvalidEmployee("{");
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void departmentFilterMatchesCaseInsensitivelyAndExactly() throws Exception {
        createEmployee("A", "a@example.com", "IT");
        createEmployee("B", "b@example.com", "HR");
        createEmployee("C", "c@example.com", "it");
        createEmployee("D", "d@example.com", null);

        mockMvc.perform(get("/api/employees").param("department", "IT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("A"))
                .andExpect(jsonPath("$[1].name").value("C"));
        mockMvc.perform(get("/api/employees").param("department", "it"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
        mockMvc.perform(get("/api/employees").param("department", " IT "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
        mockMvc.perform(get("/api/employees").param("department", "I"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
        mockMvc.perform(get("/api/employees").param("department", "Unknown"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void blankAndWhitespaceDepartmentReturnEmptyListEvenWhenEmployeesExist() throws Exception {
        createEmployee("A", "a@example.com", "IT");

        mockMvc.perform(get("/api/employees").param("department", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
        mockMvc.perform(get("/api/employees").param("department", "   "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void postWithDepartmentEchoesItAndStoresItAsGiven() throws Exception {
        mockMvc.perform(post("/api/employees")
                        .contentType(APPLICATION_JSON)
                        .content("{\"name\":\"A\",\"email\":\"a@example.com\",\"department\":\" IT \"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.department").value(" IT "));
        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.department").value(" IT "));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void postWithoutDepartmentStillSucceedsWithNullDepartment() throws Exception {
        mockMvc.perform(post("/api/employees")
                        .contentType(APPLICATION_JSON)
                        .content("{\"name\":\"A\",\"email\":\"a@example.com\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("A"))
                .andExpect(jsonPath("$.email").value("a@example.com"))
                .andExpect(jsonPath("$.department").value(org.hamcrest.Matchers.nullValue()));
    }

    private void createEmployee(String name, String email, String department) throws Exception {
        String dept = department == null ? "" : ",\"department\":\"" + department + "\"";
        mockMvc.perform(post("/api/employees")
                        .contentType(APPLICATION_JSON)
                        .content("{\"name\":\"" + name + "\",\"email\":\"" + email + "\"" + dept + "}"))
                .andExpect(status().isCreated());
    }

    private void assertInvalidEmployee(String body) throws Exception {
        mockMvc.perform(post("/api/employees")
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid employee data"));
    }
}