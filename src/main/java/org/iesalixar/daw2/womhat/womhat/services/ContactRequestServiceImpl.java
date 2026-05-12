package org.iesalixar.daw2.womhat.womhat.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.iesalixar.daw2.womhat.womhat.dtos.ContactFormDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * Implementación del servicio de contacto público.
 *
 * <p>Objetivos:
 * <ul>
 *   <li>Enviar un correo real cuando SMTP esté correctamente configurado.</li>
 *   <li>Usar textos traducibles mediante MessageSource.</li>
 *   <li>Usar Reply-To con el email que introduce el usuario.</li>
 *   <li>Escapar contenido para evitar inyección de HTML.</li>
 * </ul>
 *
 * <p>Nota honesta:
 * no es posible asegurar desde aquí que el correo del remitente "existe realmente".
 * Lo que sí hacemos es validar formato, longitud y contenido del formulario.
 */
@Service
public class ContactRequestServiceImpl implements ContactRequestService {

    private static final Logger logger = LoggerFactory.getLogger(ContactRequestServiceImpl.class);

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private MessageSource messageSource;

    @Value("${spring.mail.from:}")
    private String defaultFrom;

    @Value("${spring.mail.host:}")
    private String mailHost;

    @Value("${spring.mail.port:0}")
    private int mailPort;

    @Value("${app.contact.to:}")
    private String contactTo;

    /**
     * Envía el mensaje de contacto al buzón configurado.
     *
     * @param dto datos del formulario
     * @param locale locale actual de la petición
     */
    @Override
    public boolean sendContactMessage(ContactFormDTO dto, Locale locale) {
        logger.info("Procesando formulario de contacto (sender={})", maskEmail(dto.getEmail()));

        if (!isMailConfigurationAvailable()) {
            logger.warn("SMTP no configurado. Mensaje de contacto registrado localmente para demo: motivo={}, sender={}",
                    dto.getMotivo(), maskEmail(dto.getEmail()));
            return false;
        }

        String localizedReason = resolveReasonLabel(dto.getMotivo(), locale);
        String localizedPreference = resolvePreferenceLabel(dto.getPref(), locale);
        String subject = messageSource.getMessage(
                "msg.public.contact.mail.subject",
                new Object[]{localizedReason},
                locale
        );

        String html = buildHtml(dto, localizedReason, localizedPreference, locale);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, StandardCharsets.UTF_8.name());

            helper.setFrom(defaultFrom);
            helper.setTo(contactTo);
            helper.setReplyTo(dto.getEmail().trim());
            helper.setSubject(subject);
            helper.setText(html, true);

            mailSender.send(message);

            logger.info("Formulario de contacto enviado correctamente a {}", contactTo);
            return true;

        } catch (MessagingException ex) {
            logger.error("Error construyendo el correo de contacto: {}", ex.getMessage(), ex);
            throw new IllegalStateException("No se pudo construir el correo de contacto.", ex);
        } catch (Exception ex) {
            logger.error("Error enviando el correo de contacto: {}", ex.getMessage(), ex);
            throw new IllegalStateException("No se pudo enviar el correo de contacto.", ex);
        }
    }

    /**
     * Valida que exista una configuración mínima de correo.
     *
     * <p>Se prefiere fallar de forma clara antes que fingir un envío correcto.</p>
     */
    private boolean isMailConfigurationAvailable() {
        return contactTo != null && !contactTo.isBlank()
                && defaultFrom != null && !defaultFrom.isBlank()
                && mailHost != null && !mailHost.isBlank()
                && mailPort > 0;
    }

    /**
     * Genera el HTML del mensaje usando textos traducibles.
     */
    private String buildHtml(ContactFormDTO dto,
                             String localizedReason,
                             String localizedPreference,
                             Locale locale) {

        String title = messageSource.getMessage("msg.public.contact.mail.title", null, locale);
        String nameLabel = messageSource.getMessage("msg.public.contact.name", null, locale);
        String emailLabel = messageSource.getMessage("msg.public.contact.email", null, locale);
        String phoneLabel = messageSource.getMessage("msg.public.contact.phone", null, locale);
        String reasonLabel = messageSource.getMessage("msg.public.contact.reason", null, locale);
        String preferenceLabel = messageSource.getMessage("msg.public.contact.preference", null, locale);
        String messageLabel = messageSource.getMessage("msg.public.contact.message", null, locale);

        return """
                <html>
                  <body style="font-family:Arial,sans-serif;line-height:1.5;">
                    <h2>%s</h2>
                    <p><strong>%s:</strong> %s</p>
                    <p><strong>%s:</strong> %s</p>
                    <p><strong>%s:</strong> %s</p>
                    <p><strong>%s:</strong> %s</p>
                    <p><strong>%s:</strong> %s</p>
                    <hr>
                    <p><strong>%s:</strong></p>
                    <p>%s</p>
                  </body>
                </html>
                """.formatted(
                escape(title),
                escape(nameLabel), escape(dto.getNombre()),
                escape(emailLabel), escape(dto.getEmail()),
                escape(phoneLabel), escape(orDash(dto.getTelefono())),
                escape(reasonLabel), escape(localizedReason),
                escape(preferenceLabel), escape(localizedPreference),
                escape(messageLabel), escapeMultiline(dto.getMensaje())
        );
    }

    /**
     * Traduce el motivo enviado por el formulario.
     */
    private String resolveReasonLabel(String motivo, Locale locale) {
        if (motivo == null || motivo.isBlank()) {
            return "-";
        }

        return switch (motivo.trim().toLowerCase(Locale.ROOT)) {
            case "info", "informacion", "información", "information" ->
                    messageSource.getMessage("msg.public.contact.reason.info", null, locale);

            case "soporte", "support" ->
                    messageSource.getMessage("msg.public.contact.reason.support", null, locale);

            case "sales", "presales", "venta", "ventas", "catalog", "catalogo", "catálogo" ->
                    messageSource.getMessage("msg.public.contact.reason.sales", null, locale);

            case "incident", "incidencia" ->
                    messageSource.getMessage("msg.public.contact.reason.incident", null, locale);

            case "otros", "other" ->
                    messageSource.getMessage("msg.public.contact.reason.other", null, locale);

            default -> motivo.trim();
        };
    }

    /**
     * Traduce la preferencia de contacto enviada por el formulario.
     */
    private String resolvePreferenceLabel(String pref, Locale locale) {
        if (pref == null || pref.isBlank()) {
            return "-";
        }

        return switch (pref.trim().toLowerCase(Locale.ROOT)) {
            case "email" ->
                    messageSource.getMessage("msg.public.contact.preference.email", null, locale);

            case "tel", "telefono", "teléfono", "phone" ->
                    messageSource.getMessage("msg.public.contact.preference.phone", null, locale);

            default -> pref.trim();
        };
    }

    /**
     * Devuelve "-" cuando el valor viene vacío.
     */
    private String orDash(String value) {
        return (value == null || value.isBlank()) ? "-" : value;
    }

    /**
     * Escapado básico para evitar inyectar HTML.
     */
    private String escape(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    /**
     * Escapa texto multilinea y convierte saltos de línea a <br>.
     */
    private String escapeMultiline(String value) {
        String escaped = escape(value);
        return escaped
                .replace("\r\n", "\n")
                .replace("\r", "\n")
                .replace("\n", "<br>");
    }

    /**
     * Enmascara emails para trazas operativas sin exponer direcciones completas.
     */
    private String maskEmail(String value) {
        if (value == null || value.isBlank()) {
            return "-";
        }

        String trimmed = value.trim();
        int atIndex = trimmed.indexOf('@');
        if (atIndex <= 1 || atIndex == trimmed.length() - 1) {
            return "***";
        }

        String local = trimmed.substring(0, atIndex);
        String domain = trimmed.substring(atIndex + 1);
        return local.charAt(0) + "***@" + domain;
    }
}
