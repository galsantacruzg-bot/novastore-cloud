package com.novastore.api.service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSendException;
import org.springframework.stereotype.Service;

@Service
public class MailService {
    private static final Logger LOG = LoggerFactory.getLogger(MailService.class);
    private static final URI RESEND_ENDPOINT = URI.create("https://api.resend.com/emails");
    private static final URI GOOGLE_TOKEN_ENDPOINT = URI.create("https://oauth2.googleapis.com/token");
    private static final URI GMAIL_SEND_ENDPOINT = URI.create("https://gmail.googleapis.com/gmail/v1/users/me/messages/send");
    private static final Pattern TOKEN_PATTERN = Pattern.compile("\"access_token\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern ADDRESS_PATTERN = Pattern.compile("^[A-Z0-9._%+\\-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private final String provider;
    private final String resendApiKey;
    private final String from;
    private final String adminEmail;
    private final boolean verificationRequired;
    private final String gmailClientId;
    private final String gmailClientSecret;
    private final String gmailRefreshToken;

    public MailService(@Value("${app.mail.provider:disabled}") String provider,
                       @Value("${app.mail.resend-api-key:}") String apiKey,
                       @Value("${app.mail.from:}") String from,
                       @Value("${app.admin.email:}") String adminEmail,
                       @Value("${app.auth.email-verification-required:false}") boolean verificationRequired,
                       @Value("${app.mail.gmail-client-id:}") String gmailClientId,
                       @Value("${app.mail.gmail-client-secret:}") String gmailClientSecret,
                       @Value("${app.mail.gmail-refresh-token:}") String gmailRefreshToken) {
        this.provider = provider.trim().toLowerCase();
        this.resendApiKey = apiKey.trim();
        this.from = from.trim();
        this.adminEmail = adminEmail.trim();
        this.verificationRequired = verificationRequired;
        this.gmailClientId = gmailClientId.trim();
        this.gmailClientSecret = gmailClientSecret.trim();
        this.gmailRefreshToken = gmailRefreshToken.trim();
        if (verificationRequired) {
            switch (this.provider) {
                case "resend" -> {
                    if (resendApiKey.isEmpty() || this.from.endsWith(".invalid"))
                        throw new IllegalStateException("Para Resend configura RESEND_API_KEY y MAIL_FROM verificado.");
                }
                case "gmail-api" -> {
                    if (this.gmailClientId.isBlank() || this.gmailClientSecret.isBlank()
                            || this.gmailRefreshToken.isBlank() || !validEmail(this.from))
                        throw new IllegalStateException("Gmail API requiere GMAIL_CLIENT_ID, GMAIL_CLIENT_SECRET, GMAIL_REFRESH_TOKEN y MAIL_FROM válidos.");
                }
                default -> throw new IllegalStateException("Para verificar correos configura EMAIL_PROVIDER=resend o gmail-api.");
            }
        }
    }

    public void sendVerificationCode(String destination, String username, String code) {
        if (!verificationRequired) return;
        send(destination, "NovaStore · Verificación", "Hola " + username + ", tu código de verificación es "
                + code + ". Caduca en 10 minutos. No lo compartas.");
    }

    public void notifyAdminRegistration(String username, String email) {
        // Evitar duplicar envíos desde Gmail personal: no consumir cuota en avisos internos.
        if (provider.equals("gmail-api") || !verificationRequired || !validEmail(adminEmail)) return;
        try {
            send(adminEmail, "NovaStore · Nuevo registro", "Nuevo usuario: " + username + " (" + email + ")");
        } catch (Exception ex) {
            LOG.warn("No se pudo notificar al administrador: {}", ex.getClass().getSimpleName());
        }
    }

    private void send(String destination, String subject, String message) {
        if (!validEmail(destination)) throw new MailSendException("Dirección de correo no válida.");
        switch (provider) {
            case "resend" -> sendViaResend(destination, subject, message);
            case "gmail-api" -> sendViaGmail(destination, subject, message);
            default -> throw new MailSendException("Proveedor de correo HTTPS no configurado.");
        }
    }

    private void sendViaResend(String destination, String subject, String message) {
        String json = "{\"from\":" + quote(from) + ",\"to\":[" + quote(destination) + "]"
                + ",\"subject\":" + quote(subject) + ",\"text\":" + quote(message) + "}";
        HttpRequest request = HttpRequest.newBuilder(RESEND_ENDPOINT)
                .timeout(Duration.ofSeconds(15))
                .header("Authorization", "Bearer " + resendApiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json)).build();
        HttpResponse<String> response = request(request);
        if (response.statusCode() < 200 || response.statusCode() >= 300)
            throw new MailSendException("Resend rechazó el envío (HTTP " + response.statusCode() + ").");
    }

    private void sendViaGmail(String destination, String subject, String message) {
        String form = "client_id=" + formEncode(gmailClientId)
                + "&client_secret=" + formEncode(gmailClientSecret)
                + "&refresh_token=" + formEncode(gmailRefreshToken)
                + "&grant_type=refresh_token";
        HttpRequest tokenRequest = HttpRequest.newBuilder(GOOGLE_TOKEN_ENDPOINT)
                .timeout(Duration.ofSeconds(15))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form)).build();
        HttpResponse<String> tokenResponse = request(tokenRequest);
        if (tokenResponse.statusCode() != 200) {
            // Jamás registrar respuesta completa: puede contener detalles sensibles del proveedor.
            LOG.warn("Gmail OAuth no proporcionó access token. HTTP {}", tokenResponse.statusCode());
            throw new MailSendException("Gmail OAuth rechazó la autorización. Verifica el refresh token y el estado de OAuth.");
        }
        Matcher matcher = TOKEN_PATTERN.matcher(tokenResponse.body());
        if (!matcher.find()) throw new MailSendException("Respuesta OAuth sin access_token.");
        String accessToken = matcher.group(1);
        String mime = "From: NovaStore <" + from + ">\r\n"
                + "To: <" + destination + ">\r\n"
                + "Subject: =?UTF-8?B?" + Base64.getEncoder().encodeToString(subject.getBytes(StandardCharsets.UTF_8)) + "?=\r\n"
                + "MIME-Version: 1.0\r\n"
                + "Content-Type: text/plain; charset=UTF-8\r\n"
                + "Content-Transfer-Encoding: base64\r\n\r\n"
                + Base64.getMimeEncoder(76, new byte[]{'\r', '\n'})
                        .encodeToString(message.getBytes(StandardCharsets.UTF_8)) + "\r\n";
        String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(mime.getBytes(StandardCharsets.UTF_8));
        HttpRequest emailRequest = HttpRequest.newBuilder(GMAIL_SEND_ENDPOINT)
                .timeout(Duration.ofSeconds(20))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"raw\":" + quote(raw) + "}")).build();
        HttpResponse<String> emailResponse = request(emailRequest);
        if (emailResponse.statusCode() < 200 || emailResponse.statusCode() >= 300) {
            LOG.warn("Gmail API rechazó el correo. HTTP {}", emailResponse.statusCode());
            throw new MailSendException("Gmail API rechazó el envío (HTTP " + emailResponse.statusCode() + ").");
        }
    }

    private HttpResponse<String> request(HttpRequest request) {
        try {
            return http.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new MailSendException("Solicitud de correo interrumpida.", ex);
        } catch (IOException ex) {
            throw new MailSendException("No fue posible conectar con el proveedor de correo HTTPS.", ex);
        }
    }

    private static boolean validEmail(String value) {
        return ADDRESS_PATTERN.matcher(value).matches() && !value.contains("\r") && !value.contains("\n");
    }

    private static String formEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
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
