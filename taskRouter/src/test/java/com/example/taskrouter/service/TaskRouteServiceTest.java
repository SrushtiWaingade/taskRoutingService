package com.example.taskrouter.service;

import com.example.taskrouter.dto.MessageRequestDTO;
import com.example.taskrouter.dto.TaskRouteResponse;
import com.example.taskrouter.repository.TaskRouteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskRouteServiceTest {

    @Mock
    private TaskRouteRepository repository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private LoggingService loggingService;

    @InjectMocks
    private TaskRouteService service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "messageExchange", "message.exchange");
        ReflectionTestUtils.setField(service, "publishRetryAttempts", 1);
        ReflectionTestUtils.setField(service, "publishRetryBackoffMs", 1);
    }

    @Test
    void rejectUnsupportedChannel() {
        MessageRequestDTO request = new MessageRequestDTO("push", "to@example.com", "hello");

        assertThatThrownBy(() -> service.acceptAndRoute(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported channel");
    }

    @Test
    void suppressDuplicateByBody() {
        when(repository.existsByUniqueBody(anyString())).thenReturn(true);

        MessageRequestDTO request = new MessageRequestDTO("email", "to@example.com", "hello");
        TaskRouteResponse response = service.acceptAndRoute(request);

        assertThat(response.isDuplicate()).isTrue();
        assertThat(response.getStatus()).isEqualTo("DUPLICATE_SUPPRESSED");

        verify(repository, never()).save(any());
        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any());
    }

    @Test
    void publishSuccessRoutesMessage() {
        when(repository.existsByUniqueBody(anyString())).thenReturn(false);

        MessageRequestDTO request = new MessageRequestDTO("sms", "1234567890", "hello");
        TaskRouteResponse response = service.acceptAndRoute(request);

        assertThat(response.isDuplicate()).isFalse();
        assertThat(response.getStatus()).isEqualTo("ROUTED");
        assertThat(response.getMessageId()).isNotBlank();

        verify(repository).save(any());
        verify(rabbitTemplate).convertAndSend(eq("message.exchange"), eq("sms"), any());
    }
}

