package com.evently.twofactor.service;

import com.evently.twofactor.model.TwoFactorCode;
import com.evently.twofactor.repository.TwoFactorCodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class TwoFactorService {

    @Autowired
    private TwoFactorCodeRepository codeRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Transactional
    public void generateAndSendCode(String email) {
        // Generar un codigo aleatorio de 6 digitos
        String code = String.format("%06d", new Random().nextInt(1000000));

        // Limpiar codigos anteriores del mismo usuario
        codeRepository.deleteByEmail(email);

        // Guardar el nuevo codigo con expiracion de 5 minutos
        TwoFactorCode factorCode = new TwoFactorCode();
        factorCode.setEmail(email);
        factorCode.setCode(code);
        factorCode.setExpiryTime(LocalDateTime.now().plusMinutes(5));
        codeRepository.save(factorCode);

        // Imprimir en consola para facilitar pruebas si no se puede enviar el email
        System.out.println("\n========================================================");
        System.out.println("[2FA SERVICE] CODIGO DE VERIFICACIÓN PARA " + email + ": " + code);
        System.out.println("========================================================\n");

        // Enviar el correo
        try {
            enviarCorreo(email, code);
        } catch (Exception e) {
            System.err.println("[2FA SERVICE] Error al enviar correo SMTP (las pruebas locales pueden continuar usando el código impreso arriba): " + e.getMessage());
        }
    }

    private void enviarCorreo(String email, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Código de Verificación de 2 Pasos - Evently");
        message.setText("Hola,\n\n"
                + "Tu código de verificación de inicio de sesión de dos pasos es: " + code + "\n\n"
                + "Este código es válido por 5 minutos y solo puede ser utilizado una vez.\n\n"
                + "Si no solicitaste este código, por favor ignora este mensaje.\n\n"
                + "Atentamente,\n"
                + "El equipo de Evently");
        
        mailSender.send(message);
    }

    @Transactional
    public boolean verifyCode(String email, String codigo) {
        Optional<TwoFactorCode> optCode = codeRepository.findTopByEmailOrderByExpiryTimeDesc(email);
        if (optCode.isEmpty()) {
            return false;
        }

        TwoFactorCode factorCode = optCode.get();

        // Validar expiracion
        if (factorCode.getExpiryTime().isBefore(LocalDateTime.now())) {
            codeRepository.delete(factorCode);
            return false;
        }

        boolean equals = factorCode.getCode().equals(codigo);
        if (equals) {
            // Eliminar codigo despues de ser usado exitosamente
            codeRepository.delete(factorCode);
        }
        return equals;
    }
}
