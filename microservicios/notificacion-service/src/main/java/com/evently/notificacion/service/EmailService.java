package com.evently.notificacion.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.core.io.ByteArrayResource;

import jakarta.mail.internet.MimeMessage;
import java.util.Base64;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EmailService.class);

    @Async
    public void enviarCorreo(String destinatario, String asunto, String cuerpoHtml, String pdfBase64, String pdfNombre) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            // UTF-8 garantiza que los emojis no se rompan
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(cuerpoHtml, true);

            if (pdfBase64 != null && !pdfBase64.isEmpty()) {
                byte[] pdfBytes = Base64.getDecoder().decode(pdfBase64);
                helper.addAttachment(
                        pdfNombre != null ? pdfNombre : "entrada.pdf",
                        new ByteArrayResource(pdfBytes),
                        "application/pdf"
                );
                log.info("📎 Adjunto PDF añadido al correo para {}", destinatario);
            }

            mailSender.send(message);
            log.info("✉️ Correo asíncrono enviado exitosamente a {}", destinatario);
        } catch (Exception e) {
            log.error("⚠️ Error al enviar correo asíncrono a {}: {}", destinatario, e.getMessage(), e);
        }
    }
}

