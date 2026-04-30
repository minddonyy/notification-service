package com.minsun.notification.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsun.notification.application.NotificationProcessor;
import com.minsun.notification.application.NotificationScheduler;
import com.minsun.notification.infrastructure.NotificationRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class NotificationControllerIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired NotificationRepository repository;

    // 스케줄러/프로세서를 Mock으로 대체해 테스트 중 비동기 처리 간섭 방지
    @MockBean NotificationScheduler notificationScheduler;
    @MockBean NotificationProcessor notificationProcessor;

    @AfterEach
    void cleanup() {
        repository.deleteAll();
    }

    private static final String REGISTER_BODY = """
            {
              "recipientId": 42,
              "notificationType": "ENROLLMENT_COMPLETE",
              "referenceType": "COURSE",
              "referenceId": 100,
              "channel": "EMAIL"
            }
            """;

    @Test
    @DisplayName("신규 알림 등록 → 201 Created, isNew=true")
    void 신규_알림_등록_201() throws Exception {
        mockMvc.perform(post("/api/v1/notifications")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REGISTER_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.isNew").value(true))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.idempotencyKey").value("42:ENROLLMENT_COMPLETE:COURSE:100:EMAIL"));
    }

    @Test
    @DisplayName("동일 요청 중복 전송 → 200 OK, isNew=false")
    void 중복_알림_등록_200() throws Exception {
        mockMvc.perform(post("/api/v1/notifications")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REGISTER_BODY))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/notifications")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REGISTER_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isNew").value(false));
    }

    @Test
    @DisplayName("알림 단건 조회 → 200 OK")
    void 단건_조회_200() throws Exception {
        String response = mockMvc.perform(post("/api/v1/notifications")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REGISTER_BODY))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(response).get("notificationId").asLong();

        mockMvc.perform(get("/api/v1/notifications/{id}", id)
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notificationId").value(id))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("존재하지 않는 알림 조회 → 404 Not Found")
    void 단건_조회_404() throws Exception {
        mockMvc.perform(get("/api/v1/notifications/99999")
                        .header("X-User-Id", "1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("수신자별 알림 목록 조회 → 200 OK, 페이징 정보 포함")
    void 목록_조회_200() throws Exception {
        mockMvc.perform(post("/api/v1/notifications")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REGISTER_BODY))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/notifications")
                        .header("X-User-Id", "1")
                        .param("recipientId", "42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.page").value(0));
    }

    @Test
    @DisplayName("필수 파라미터 누락 시 400 Bad Request")
    void 필수파라미터_누락_400() throws Exception {
        mockMvc.perform(post("/api/v1/notifications")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
