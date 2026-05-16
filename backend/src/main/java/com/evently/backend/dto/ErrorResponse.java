package com.evently.backend.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ErrorResponse {

    private String error;
    private int codigo;
    private LocalDateTime timestamp;

    public ErrorResponse(String error, int codigo) {
        this.error = error;
        this.codigo = codigo;
        this.timestamp = LocalDateTime.now();
    }
}
