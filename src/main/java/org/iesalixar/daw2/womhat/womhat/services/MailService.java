package org.iesalixar.daw2.womhat.womhat.services;

import java.util.Locale;
import java.util.Map;

/**
 * Servicio genérico para envío de correos de la aplicación.
 */
public interface MailService {

    /**
     * Envía un correo en texto plano.
     */
    void sendText(String to, String subject, String text);

    /**
     * Envía un correo HTML.
     */
    void sendHtml(String to, String subject, String html);

    /**
     * Envía un correo HTML a partir de una plantilla Thymeleaf.
     */
    void sendTemplate(String to,
                      String subjectKey,
                      String templateName,
                      Map<String, Object> variables,
                      Locale locale);
}