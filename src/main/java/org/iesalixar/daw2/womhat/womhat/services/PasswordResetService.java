package org.iesalixar.daw2.womhat.womhat.services;

/**
 * Servicio para la recuperación de contraseña.
 */
public interface PasswordResetService {

    /**
     * Solicita el envío de un enlace de recuperación.
     *
     * @param email email del usuario
     * @param requestIp IP origen de la solicitud
     * @param userAgent user agent de la petición
     */
    void requestPasswordReset(String email, String requestIp, String userAgent);

    /**
     * Resetea la contraseña a partir de un token válido.
     *
     * @param rawToken token en claro
     * @param newPassword nueva contraseña
     */
    void resetPassword(String rawToken, String newPassword);
}