package com.evently.notificacion.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TwilioWhatsAppService {

    private static final Logger log = LoggerFactory.getLogger(TwilioWhatsAppService.class);

    @Value("${twilio.account-sid:}")
    private String accountSid;

    @Value("${twilio.auth-token:}")
    private String authToken;

    @Value("${twilio.whatsapp-from:whatsapp:+14155238886}")
    private String whatsappFrom;

    private boolean configurado = false;

    @PostConstruct
    public void init() {
        if (accountSid != null && !accountSid.isEmpty()
                && !accountSid.contains("TU_TWILIO")
                && authToken != null && !authToken.isEmpty()
                && !authToken.contains("TU_TWILIO")) {
            try {
                Twilio.init(accountSid, authToken);
                configurado = true;
                log.info("✅ Twilio WhatsApp inicializado correctamente");
            } catch (Exception e) {
                log.warn("⚠️ Error al inicializar Twilio: {}", e.getMessage());
                configurado = false;
            }
        } else {
            log.warn("⚠️ Twilio no configurado. Las notificaciones de WhatsApp se omitirán.");
        }
    }

    public boolean enviarWhatsApp(String telefonoDestino, String mensaje) {
        if (!configurado) {
            log.info("📱 [SIMULADO] WhatsApp a {}: {}", telefonoDestino, mensaje);
            return false;
        }

        try {
            String to = telefonoDestino.startsWith("whatsapp:")
                    ? telefonoDestino
                    : "whatsapp:" + telefonoDestino;

            Message message = Message.creator(
                    new PhoneNumber(to),
                    new PhoneNumber(whatsappFrom),
                    mensaje
            ).create();

            log.info("✅ WhatsApp enviado a {} | SID: {}", telefonoDestino, message.getSid());
            return true;
        } catch (Exception e) {
            log.error("❌ Error al enviar WhatsApp a {}: {}", telefonoDestino, e.getMessage());
            return false;
        }
    }

    public boolean isConfigurado() {
        return configurado;
    }
}
