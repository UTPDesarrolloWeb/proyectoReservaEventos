package com.evently.twofactor.controller;

import com.evently.twofactor.dto.SendCodeRequest;
import com.evently.twofactor.dto.VerifyCodeRequest;
import com.evently.twofactor.service.TwoFactorService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/2fa")
public class TwoFactorController {

    @Autowired
    private TwoFactorService twoFactorService;

    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> sendCode(@Valid @RequestBody SendCodeRequest request) {
        twoFactorService.generateAndSendCode(request.getEmail());
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Código de verificación enviado exitosamente");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyCode(@Valid @RequestBody VerifyCodeRequest request) {
        boolean valido = twoFactorService.verifyCode(request.getEmail(), request.getCodigo());
        Map<String, Object> response = new HashMap<>();
        response.put("valido", valido);
        if (valido) {
            response.put("mensaje", "Código de verificación correcto");
            return ResponseEntity.ok(response);
        } else {
            response.put("mensaje", "Código de verificación incorrecto o expirado");
            return ResponseEntity.badRequest().body(response);
        }
    }
}
