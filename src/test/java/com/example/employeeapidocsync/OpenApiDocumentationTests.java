package com.example.employeeapidocsync;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.TEXT_HTML;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OpenApiDocumentationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void swaggerUiAndOpenApiDocumentAreAvailableForOnlyTheExistingOperations() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(TEXT_HTML));

        JsonNode document = getOpenApiDocument();
        assertThat(document.path("openapi").asText()).startsWith("3.");

        JsonNode paths = document.path("paths");
        assertThat(fieldNames(paths)).containsExactlyInAnyOrder(
                "/api/employees", "/api/employees/{id}");
        assertThat(fieldNames(paths.path("/api/employees"))).containsExactlyInAnyOrder("get", "post");
        assertThat(fieldNames(paths.path("/api/employees/{id}"))).containsExactly("get");
    }

    @Test
    void documentsRequestAndResponseSchemasWithTheirExistingFieldsAndTypes() throws Exception {
        JsonNode schemas = getOpenApiDocument().path("components").path("schemas");

        assertSchemaProperties(schemas, "Employee", Set.of("id", "name", "email"));
        assertThat(schemas.path("Employee").path("properties").path("id").path("type").asText())
                .isEqualTo("integer");
        assertThat(schemas.path("Employee").path("properties").path("id").path("format").asText())
                .isEqualTo("int64");
        assertStringProperty(schemas, "Employee", "name");
        assertStringProperty(schemas, "Employee", "email");

        assertSchemaProperties(schemas, "CreateEmployeeRequest", Set.of("name", "email"));
        assertStringProperty(schemas, "CreateEmployeeRequest", "name");
        assertStringProperty(schemas, "CreateEmployeeRequest", "email");

        assertSchemaProperties(schemas, "ErrorResponse", Set.of("error"));
        assertStringProperty(schemas, "ErrorResponse", "error");
    }

    @Test
    void documentsExactResponseStatusesAndSchemasForEachOperation() throws Exception {
        JsonNode paths = getOpenApiDocument().path("paths");

        JsonNode collectionGet = paths.path("/api/employees").path("get");
        assertThat(fieldNames(collectionGet.path("responses"))).containsExactly("200");
        JsonNode collectionSchema = responseSchema(collectionGet, "200");
        assertThat(collectionSchema.path("type").asText()).isEqualTo("array");
        assertThat(collectionSchema.path("items").path("$ref").asText())
                .isEqualTo("#/components/schemas/Employee");

        JsonNode byIdGet = paths.path("/api/employees/{id}").path("get");
        assertThat(fieldNames(byIdGet.path("responses"))).containsExactlyInAnyOrder("200", "404");
        assertResponseRef(byIdGet, "200", "Employee");
        assertResponseRef(byIdGet, "404", "ErrorResponse");

        JsonNode createPost = paths.path("/api/employees").path("post");
        assertThat(fieldNames(createPost.path("responses"))).containsExactlyInAnyOrder("201", "400");
        assertResponseRef(createPost, "201", "Employee");
        assertResponseRef(createPost, "400", "ErrorResponse");
        assertThat(createPost.path("requestBody").path("required").asBoolean()).isTrue();
        assertThat(createPost.path("requestBody").path("content").path("application/json")
                .path("schema").path("$ref").asText())
                .isEqualTo("#/components/schemas/CreateEmployeeRequest");
    }

    private JsonNode getOpenApiDocument() throws Exception {
        MvcResult result = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private static void assertResponseRef(JsonNode operation, String status, String schema) {
        assertThat(responseSchema(operation, status).path("$ref").asText())
                .isEqualTo("#/components/schemas/" + schema);
    }

    private static JsonNode responseSchema(JsonNode operation, String status) {
        return operation.path("responses").path(status).path("content").path("application/json")
                .path("schema");
    }

    private static void assertSchemaProperties(JsonNode schemas, String schema, Set<String> expected) {
        assertThat(fieldNames(schemas.path(schema).path("properties")))
                .containsExactlyInAnyOrderElementsOf(expected);
    }

    private static void assertStringProperty(JsonNode schemas, String schema, String property) {
        assertThat(schemas.path(schema).path("properties").path(property).path("type").asText())
                .isEqualTo("string");
    }

    private static Set<String> fieldNames(JsonNode node) {
        Set<String> names = new HashSet<>();
        node.fieldNames().forEachRemaining(names::add);
        return names;
    }
}
