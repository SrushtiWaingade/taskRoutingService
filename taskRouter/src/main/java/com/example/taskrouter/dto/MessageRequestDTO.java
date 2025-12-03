package com.example.taskrouter.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageRequestDTO {
    @NotBlank(message = "channel is required") // email | sms | whatsapp
    private String channel;

    @NotBlank(message = "to is required")
    private String to;

    @NotBlank(message = "body is required")
    private String body;
}
