package com.novastore.api.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSendException;
import org.springframework.stereotype.Service;

@Service
public class MailService {
    private static final Logger LOG = LoggerFactory.getLogger(MailService.class);
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private final String provider;
    private final String apiKey;
    private final String from;
    private final String adminEmail;
    private final boolean verificationRequired;

    public MailService(@Value("${app.mail.provider:disabled}") String provider,
                       @Value("${app.mail.resend-api-key:}") String apiKey,
                       @Value("${app.mail.from:}") String from,
                       @Value("${app.admin.email:}") String adminEmail,
                       @Value("${app.auth.email-verification-required:false}") boolean verificationRequired) {
        this.provider = provider.trim().toLowerCase();
        this.apiKey = apiKey.trim();
        this.from = from.trim();
        this.adminEmail = adminEmail.trim();
        this.verificationRequired = verificationRequired;
        if (verificationRequired && (!this.provider.equals("resend") || this.apiKey.isEmpty()
                || this.from.endsWith(".invalid"))) {
            throw new IllegalStateException("Para verificar correos configura EMAIL_PROVIDER=resend, RESEND_API_KEY y MAIL_FROM con dominio verificado.");
        }
    }

    public void sendVerificationCode(String destination, String username, String code) {
        if (!verificationRequired) return;
        send(destination, "NovaStore · Verificación", "Hola " + username + ", tu código de verificación es "
                + code + ". Caduca en 10 minutos. No lo compartas.");
    }

    public void notifyAdminRegistration(String username, String email) {
        if (!provider.equals("resend") || adminEmail.endsWith(".invalid") || apiKey.isEmpty()) return;
        try {
            send(adminEmail, "NovaStore · Nuevo registro", "Nuevo usuario: " + username + " (" + email + ")");
        } catch (Exception ex) {
            LOG.warn("No se pudo notificar registro al administrador: {}", ex.getClass().getSimpleName());
        }
    }

    private void send(String destination, String subject, String message) {
        if (!provider.equals("resend")) throw new MailSendException("Proveedor de correo HTTPS no configurado.");
        String json = "{\"from\":" + quote(from) + ",\"to\":[" + quote(destination) + "]"
                + ",\"subject\":" + quote(subject) + ",\"text\":" + quote(message) + "}";
        HttpRequest request = HttpRequest.newBuilder(URI.create("https://api.resend.com/emails"))
                .timeout(Duration.ofSeconds(15))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json)).build();
        try {
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                LOG.warn("Proveedor de email HTTPS respondió HTTP {}", response.statusCode());
                throw new MailSendException("Servicio de correo HTTPS no disponible (HTTP " + response.statusCode() + ").");
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new MailSendException("Envío de correo interrumpido.", ex);
        } catch (IOException ex) {
            throw new MailSendException("No se pudo conectar con el proveedor de correo HTTPS.", ex);
        }
    }

    private static String quote(String value) {
        StringBuilder result = new StringBuilder("\"");
        for (char ch : value.toCharArray()) {
            switch (ch) {
                case '"' -> result.append("\\\"");
                case '\\' -> result.append("\\\\");
                case '\n' -> result.append("\\n");
                case '\r' -> result.append("\\r");
                case '\t' -> result.append("\\t");
                default -> {
                    if (ch < 0x20) result.append(String.format("\\u%04x", (int) ch));
                    else result.append(ch);
                }
            }
        }
        return result.append('"').toString();
    }
}
