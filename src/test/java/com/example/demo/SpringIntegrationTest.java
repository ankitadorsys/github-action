package com.example.demo;

import com.example.demo.api.model.CategoryRequest;
import com.example.demo.api.model.CommentRequest;
import com.example.demo.api.model.TagRequest;
import com.example.demo.api.model.TaskRequest;
import com.example.demo.api.model.TaskStatus;
import com.example.demo.api.model.TaskPriority;
import tools.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@ActiveProfiles("nosecurity")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Spring Integration Tests - Full Stack with H2")
class SpringIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    // --- Hello endpoint ---

    @Test
    @Order(1)
    @DisplayName("GET /api/hello - returns greeting from live server")
    void helloEndpoint_returnsGreeting() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/hello", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Hello, World!");
    }

    // --- Category CRUD ---

    @Test
    @Order(10)
    @DisplayName("POST /api/categories - creates a category in H2")
    void createCategory() throws Exception {
        CategoryRequest request = new CategoryRequest("Work").color("#FF0000");

        ResponseEntity<String> response = postJson("/api/categories", request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).contains("\"name\":\"Work\"");
        assertThat(response.getBody()).contains("\"id\":");
    }

    @Test
    @Order(11)
    @DisplayName("GET /api/categories - lists categories from H2")
    void getAllCategories() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/categories", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Work");
    }

    @Test
    @Order(12)
    @DisplayName("GET /api/categories/1 - retrieves single category")
    void getCategoryById() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/categories/1", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"name\":\"Work\"");
        assertThat(response.getBody()).contains("\"taskCount\":");
    }

    @Test
    @Order(13)
    @DisplayName("PUT /api/categories/1 - updates category in H2")
    void updateCategory() throws Exception {
        CategoryRequest request = new CategoryRequest("Personal").color("#00FF00");

        ResponseEntity<String> response = putJson("/api/categories/1", request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"name\":\"Personal\"");
    }

    @Test
    @Order(14)
    @DisplayName("GET /api/categories/999 - returns 404 for non-existent category")
    void getCategoryById_notFound() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/categories/999", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // --- Tag CRUD ---

    @Test
    @Order(20)
    @DisplayName("POST /api/tags - creates a tag in H2")
    void createTag() throws Exception {
        TagRequest request = new TagRequest("urgent");

        ResponseEntity<String> response = postJson("/api/tags", request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).contains("\"name\":\"urgent\"");
    }

    @Test
    @Order(21)
    @DisplayName("POST /api/tags - creates a second tag")
    void createSecondTag() throws Exception {
        TagRequest request = new TagRequest("important");

        ResponseEntity<String> response = postJson("/api/tags", request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).contains("\"name\":\"important\"");
    }

    @Test
    @Order(22)
    @DisplayName("GET /api/tags - lists all tags")
    void getAllTags() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/tags", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("urgent");
        assertThat(response.getBody()).contains("important");
    }

    @Test
    @Order(23)
    @DisplayName("PUT /api/tags/1 - updates tag name")
    void updateTag() throws Exception {
        TagRequest request = new TagRequest("critical");

        ResponseEntity<String> response = putJson("/api/tags/1", request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"name\":\"critical\"");
    }

    // --- Task CRUD (with relationships) ---

    @Test
    @Order(30)
    @DisplayName("POST /api/tasks - creates task with category and tags")
    void createTask() throws Exception {
        TaskRequest request = new TaskRequest("Build REST API")
                .description("Implement CRUD endpoints for task manager")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.HIGH)
                .dueDate(LocalDate.of(2026, 5, 1))
                .categoryId(1L)
                .tagIds(Set.of(1L, 2L));

        ResponseEntity<String> response = postJson("/api/tasks", request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).contains("\"title\":\"Build REST API\"");
        assertThat(response.getBody()).contains("\"status\":\"TODO\"");
        assertThat(response.getBody()).contains("\"priority\":\"HIGH\"");
        assertThat(response.getBody()).contains("\"category\":");
        assertThat(response.getBody()).contains("\"tags\":");
    }

    @Test
    @Order(31)
    @DisplayName("POST /api/tasks - creates task with defaults (no category/tags)")
    void createTaskWithDefaults() throws Exception {
        TaskRequest request = new TaskRequest("Simple task");

        ResponseEntity<String> response = postJson("/api/tasks", request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).contains("\"title\":\"Simple task\"");
        assertThat(response.getBody()).contains("\"status\":\"TODO\"");
        assertThat(response.getBody()).contains("\"priority\":\"MEDIUM\"");
    }

    @Test
    @Order(32)
    @DisplayName("GET /api/tasks - lists all tasks")
    void getAllTasks() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/tasks", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Build REST API");
        assertThat(response.getBody()).contains("Simple task");
    }

    @Test
    @Order(33)
    @DisplayName("GET /api/tasks/1 - retrieves task with relationships populated")
    void getTaskById() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/tasks/1", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"title\":\"Build REST API\"");
        assertThat(response.getBody()).contains("\"commentCount\":");
    }

    @Test
    @Order(34)
    @DisplayName("PUT /api/tasks/1 - updates task status and priority")
    void updateTask() throws Exception {
        TaskRequest request = new TaskRequest("Build REST API")
                .description("CRUD endpoints - in progress")
                .status(TaskStatus.IN_PROGRESS)
                .priority(TaskPriority.HIGH)
                .categoryId(1L)
                .tagIds(Set.of(1L));

        ResponseEntity<String> response = putJson("/api/tasks/1", request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"status\":\"IN_PROGRESS\"");
    }

    @Test
    @Order(35)
    @DisplayName("GET /api/tasks/999 - returns 404 for non-existent task")
    void getTaskById_notFound() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/tasks/999", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // --- Comment CRUD (nested under task) ---

    @Test
    @Order(40)
    @DisplayName("POST /api/tasks/1/comments - adds comment to task")
    void addComment() throws Exception {
        CommentRequest request = new CommentRequest("Looking good!", "Ankit");

        ResponseEntity<String> response = postJson("/api/tasks/1/comments", request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).contains("\"content\":\"Looking good!\"");
        assertThat(response.getBody()).contains("\"authorName\":\"Ankit\"");
        assertThat(response.getBody()).contains("\"taskId\":1");
    }

    @Test
    @Order(41)
    @DisplayName("POST /api/tasks/1/comments - adds second comment")
    void addSecondComment() throws Exception {
        CommentRequest request = new CommentRequest("Almost done!", "Reviewer");

        ResponseEntity<String> response = postJson("/api/tasks/1/comments", request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    @Order(42)
    @DisplayName("GET /api/tasks/1/comments - lists all comments for task")
    void getCommentsByTaskId() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/tasks/1/comments", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Looking good!");
        assertThat(response.getBody()).contains("Almost done!");
    }

    @Test
    @Order(43)
    @DisplayName("GET /api/tasks/999/comments - returns 404 for non-existent task")
    void getCommentsByTaskId_taskNotFound() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/tasks/999/comments", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @Order(44)
    @DisplayName("GET /api/tasks/1 - commentCount reflects added comments")
    void taskCommentCountUpdated() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/tasks/1", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"commentCount\":2");
    }

    // --- Deletes (run last to avoid breaking earlier tests) ---

    @Test
    @Order(90)
    @DisplayName("DELETE /api/comments/1 - deletes a comment")
    void deleteComment() {
        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/comments/1", HttpMethod.DELETE, null, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @Order(91)
    @DisplayName("DELETE /api/comments/999 - returns 404 for non-existent comment")
    void deleteComment_notFound() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/comments/999", HttpMethod.DELETE, null, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @Order(92)
    @DisplayName("DELETE /api/tasks/2 - deletes a task")
    void deleteTask() {
        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/tasks/2", HttpMethod.DELETE, null, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @Order(93)
    @DisplayName("DELETE /api/tags/2 - deletes a tag")
    void deleteTag() {
        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/tags/2", HttpMethod.DELETE, null, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @Order(94)
    @DisplayName("DELETE /api/tasks/1 - deletes task (cascades to remaining comments)")
    void deleteTaskCascadesComments() {
        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/tasks/1", HttpMethod.DELETE, null, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @Order(95)
    @DisplayName("DELETE /api/categories/1 - deletes category after tasks removed")
    void deleteCategory() {
        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/categories/1", HttpMethod.DELETE, null, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @Order(96)
    @DisplayName("GET /api/tasks - returns empty list after all deletions")
    void allTasksDeleted() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/tasks", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("[]");
    }

    // --- Actuator ---

    @Test
    @Order(99)
    @DisplayName("GET /actuator/health - returns UP status")
    void actuatorHealth() {
        ResponseEntity<String> response = restTemplate.getForEntity("/actuator/health", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"status\":\"UP\"");
    }

    // --- Helper methods ---

    private ResponseEntity<String> postJson(String url, Object body) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(body), headers);
        return restTemplate.postForEntity(url, entity, String.class);
    }

    private ResponseEntity<String> putJson(String url, Object body) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(body), headers);
        return restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);
    }
}
