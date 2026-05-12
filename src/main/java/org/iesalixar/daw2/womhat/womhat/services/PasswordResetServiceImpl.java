package org.iesalixar.daw2.womhat.womhat.services;

import lombok.RequiredArgsConstructor;
import org.iesalixar.daw2.womhat.womhat.entities.PasswordResetToken;
import org.iesalixar.daw2.womhat.womhat.entities.User;
import org.iesalixar.daw2.womhat.womhat.repositories.PasswordResetTokenRepository;
import org.iesalixar.daw2.womhat.womhat.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Locale;
import java.util.Map;

/**
 * Implementación del flujo de recuperación de contraseña.
 *
 * Estrategia:
 * - nunca se guarda el token en claro,
 * - se almacena solo su hash,
 * - se invalida cualquier token anterior activo del usuario.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements PasswordResetService {

    private static final Logger logger = LoggerFactory.getLogger(PasswordResetServiceImpl.class);
    private static final int TOKEN_BYTES = 32;
    private static final int TOKEN_TTL_MINUTES = 30;
    private static final int PASSWORD_EXPIRY_DAYS = 90;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MailService mailService;

    @Autowired
    private AppUrlService appUrlService;

    @Value("${spring.mail.host:}")
    private String mailHost;

    @Value("${spring.mail.port:0}")
    private int mailPort;

    @Value("${spring.mail.from:}")
    private String defaultFrom;

    /**
     * Solicita un reset de contraseña.
     *
     * No revela al exterior si el email existe o no.
     */
    @Override
    public void requestPasswordReset(String email, String requestIp, String userAgent) {
        Locale locale = LocaleContextHolder.getLocale();
        LocalDateTime now = LocalDateTime.now();

        User user = userRepository.findByEmailIgnoreCase(email).orElse(null);

        // Respuesta neutra para evitar enumeración de usuarios.
        if (user == null) {
            logger.info("Solicitud de reset recibida para email no registrado (email={}). Respuesta neutra aplicada.",
                    maskEmail(email));
            return;
        }

        if (!isMailConfigurationAvailable()) {
            logger.warn("SMTP no configurado. Solicitud de reset registrada sin envío real (email={}).",
                    maskEmail(email));
            return;
        }

        // Invalida tokens anteriores sin usar bulk update masivo.
        var activeTokens = tokenRepository.findAllByUser_IdAndUsedAtIsNull(user.getId());
        if (!activeTokens.isEmpty()) {
            activeTokens.forEach(existing -> existing.setUsedAt(now));
            tokenRepository.saveAll(activeTokens);
        }

        String rawToken = generateSecureToken();
        String tokenHash = sha256Hex(rawToken);

        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setTokenHash(tokenHash);
        token.setCreatedAt(now);
        token.setExpiresAt(now.plusMinutes(TOKEN_TTL_MINUTES));
        token.setRequestIp(requestIp);
        token.setUserAgent(safeTruncate(userAgent, 255));

        tokenRepository.save(token);
        logger.info("Token de reset generado para userId={} desde ip={}", user.getId(), requestIp);

        String resetUrl = appUrlService.buildResetUrl(rawToken);

        Map<String, Object> variables = Map.of(
                "resetUrl", resetUrl,
                "ttlMinutes", TOKEN_TTL_MINUTES,
                "logoUrl", appUrlService.buildUrl("/assets/images/Logo1-mail.png", Map.of())
        );

        mailService.sendTemplate(
                user.getEmail(),
                "mail.passwordreset.subject",
                "mail/password-reset",
                variables,
                locale
        );
        logger.info("Correo de reset solicitado para userId={}", user.getId());
    }

    /**
     * Resetea la contraseña del usuario si el token es válido.
     */
    @Override
    public void resetPassword(String rawToken, String newPassword) {
        LocalDateTime now = LocalDateTime.now();
        String tokenHash = sha256Hex(rawToken);

        PasswordResetToken token = tokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> {
                    logger.warn("Intento de reset con token inexistente.");
                    return new IllegalArgumentException("Token inválido.");
                });

        if (token.isUsed() || token.isExpired()) {
            logger.warn("Intento de reset con token usado o expirado para userId={}", token.getUser() != null ? token.getUser().getId() : null);
            throw new IllegalArgumentException("El token no es válido o ha expirado.");
        }

        User user = token.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setLastPasswordChange(now);
        user.setPasswordExpiresAt(now.plusDays(PASSWORD_EXPIRY_DAYS));
        user.setMustChangePassword(Boolean.FALSE);
        user.setFailedLoginAttempts(0);
        user.setAccountNonLocked(Boolean.TRUE);

        token.setUsedAt(now);

        userRepository.save(user);
        tokenRepository.save(token);
        logger.info("Contraseña restablecida para userId={}. Se reinician flags de bloqueo/caducidad.", user.getId());

        // Invalida cualquier otro token que pudiera quedar activo.
        var remainingActiveTokens = tokenRepository.findAllByUser_IdAndUsedAtIsNull(user.getId());
        if (!remainingActiveTokens.isEmpty()) {
            remainingActiveTokens.forEach(existing -> existing.setUsedAt(now));
            tokenRepository.saveAll(remainingActiveTokens);
        }
    }

    /**
     * Genera un token seguro, apto para URL.
     */
    private String generateSecureToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Calcula el SHA-256 en hexadecimal del token.
     */
    private String sha256Hex(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(raw.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("No se pudo calcular el hash del token.", e);
        }
    }

    /**
     * Recorta un texto a una longitud máxima.
     */
    private String safeTruncate(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }

    private boolean isMailConfigurationAvailable() {
        return mailHost != null && !mailHost.isBlank()
                && mailPort > 0
                && defaultFrom != null && !defaultFrom.isBlank();
    }

    /**
     * Enmascara emails en trazas.
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
