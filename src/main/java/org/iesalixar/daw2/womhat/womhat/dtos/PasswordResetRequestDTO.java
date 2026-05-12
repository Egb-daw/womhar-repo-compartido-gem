package org.iesalixar.daw2.womhat.womhat.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO del formulario "he olvidado mi contraseña".
 *
 * Solo pide el email del usuario para iniciar el flujo
 * de recuperación de contraseña.
 */
@Data
public class PasswordResetRequestDTO {

    /** Correo del usuario que solicita el reseteo. */
    @NotBlank(message = "{msg.password-reset.request.email.notblank}")
    @Email(message = "{msg.password-reset.request.email.invalid}")
    @Size(max = 100, message = "{msg.password-reset.request.email.size}")
    private String email;
}
