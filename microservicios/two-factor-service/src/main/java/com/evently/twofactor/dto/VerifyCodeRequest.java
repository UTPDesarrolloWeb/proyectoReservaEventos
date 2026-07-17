package com.evently.twofactor.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyCodeRequest {
    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String codigo;
}
