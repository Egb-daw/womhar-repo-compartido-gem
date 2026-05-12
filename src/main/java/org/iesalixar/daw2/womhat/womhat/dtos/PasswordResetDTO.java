package org.iesalixar.daw2.womhat.womhat.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO del formulario final de reseteo de contraseña.
 *
 * Incluye el token recibido y la nueva contraseña
 * con su confirmación.
 */
@Data
public class PasswordResetDTO {

    /** Token recibido por email para validar el reseteo. */
    @NotBlank(message = "{msg.password-reset.token.notblank}")
    private String token;

    /** Nueva contraseña en texto plano antes de hashearla. */
    @NotBlank(message = "{msg.password-reset.newPassword.notblank}")
    @Size(min = 8, max = 72, message = "{msg.password-reset.newPassword.size}")
    private String newPassword;

    /** Confirmación de la nueva contraseña. */
    @NotBlank(message = "{msg.password-reset.confirmPassword.notblank}")
    @Size(min = 8, max = 72, message = "{msg.password-reset.confirmPassword.size}")
    private String confirmPassword;
}
