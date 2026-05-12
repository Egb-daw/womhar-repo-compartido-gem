package org.iesalixar.daw2.womhat.womhat.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;

/**
 * Implementación del servicio de correo.
 *
 * Permite enviar:
 * - texto plano,
 * - HTML directo,
 * - HTML a través de plantillas Thymeleaf.
 */
@Service
public class MailServiceImpl implements MailService {

    private static final Logger logger = LoggerFactory.getLogger(MailServiceImpl.class);

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private SpringTemplateEngine templateEngine;

    /**
     * Remitente por defecto configurado en propiedades.
     */
    @Value("${spring.mail.from:}")
    private String defaultFrom;

    /**
     * Envía un correo en texto plano.
     */
    @Override
    public void sendText(String to, String subject, String text) {
        send(to, subject, text, false);
    }

    /**
     * Envía un correo en HTML.
     */
    @Override
    public void sendHtml(String to, String subject, String html) {
        send(to, subject, html, true);
    }

    /**
     * Envía un correo a partir de una plantilla Thymeleaf.
     *
     * @param to destinatario
     * @param subjectKey clave i18n del asunto
     * @param templateName plantilla Thymeleaf
     * @param variables variables de plantilla
     * @param locale locale actual
     */
    @Override
    public void sendTemplate(String to,
                             String subjectKey,
                             String templateName,
                             Map<String, Object> variables,
                             Locale locale) {

        String subject = messageSource.getMessage(subjectKey, null, locale);

        Context context = new Context(locale);
        context.setVariables(variables);
        context.setVariable("subject", subject);
        context.setVariable("lang", locale.getLanguage());

        String html = templateEngine.process(templateName, context);
        logger.info("Preparando envío de correo por plantilla: to={}, template={}", to, templateName);
        send(to, subject, html, true);
    }

    /**
     * Método interno común para enviar el correo.
     */
    private void send(String to, String subject, String body, boolean isHtml) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, StandardCharsets.UTF_8.name());

            if (defaultFrom != null && !defaultFrom.isBlank()) {
                helper.setFrom(defaultFrom);
            } else {
                logger.warn("spring.mail.from no configurado. El proveedor SMTP decidirá el remitente efectivo.");
            }

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, isHtml);

            mailSender.send(message);
            logger.info("Correo enviado: to={}, html={}", to, isHtml);

        } catch (MessagingException e) {
            logger.error("Error construyendo correo para {}: {}", to, e.getMessage(), e);
            throw new IllegalStateException("No se pudo enviar el correo.", e);
        } catch (Exception e) {
            logger.error("Error enviando correo para {}: {}", to, e.getMessage(), e);
            throw new IllegalStateException("No se pudo enviar el correo.", e);
        }
    }
}
