package com.evently.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class Verify2faRequest {
    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String codigo;
}
