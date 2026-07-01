package com.example.analytics.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Request payload for {@code POST /track}.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackEventRequest {

    @NotBlank(message = "userId must not be blank")
    private String userId;

    @NotBlank(message = "event must not be blank")
    private String event;

    private JsonNode metadata;

    /** Optional. Defaults to "now" on the server when not supplied. */
    private LocalDateTime timestamp;
}
