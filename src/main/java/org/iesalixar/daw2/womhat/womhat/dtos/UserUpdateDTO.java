package org.iesalixar.daw2.womhat.womhat.dtos;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * DTO para actualizar usuarios existentes.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateDTO {

    /** ID del usuario que se va a editar. */
    @NotNull(message = "{msg.user-form.id.notnull}")
    private Long id;

    /** Email del usuario. */
    @Email(message = "{msg.user-form.email.invalid}")
    @NotBlank(message = "{msg.user-form.email.notblank}")
    @Size(max = 100, message = "{msg.user-form.email.size}")
    private String email;

    /** Estado activo/inactivo. */
    private boolean active;

    /** Estado de bloqueo de la cuenta. */
    private boolean accountNonLocked;

    /** Último cambio de contraseña. */
    @PastOrPresent(message = "{msg.user-form.lastPasswordChange.pastOrPresent}")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime lastPasswordChange;

    /** Caducidad de la contraseña. */
    @FutureOrPresent(message = "{msg.user-form.passwordExpiresAt.futureOrPresent}")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime passwordExpiresAt;

    /** Número de intentos fallidos. */
    @Min(value = 0, message = "{msg.user-form.failedLoginAttempts.min}")
    private Integer failedLoginAttempts = 0;

    /** Indica si el email está verificado. */
    private boolean emailVerified;

    /** Indica si debe cambiar contraseña. */
    private boolean mustChangePassword;

    /** Roles seleccionados para el usuario. */
    @NotEmpty(message = "{msg.user-form.roles.notempty}")
    private Set<Long> roleIds = new HashSet<>();

    /**
     * Contraseña opcional.
     * Si viene vacía o null, no se cambia.
     */
    @Pattern(regexp = "^\\s*$|.{8,72}$", message = "{msg.user-form.password.size}")
    private String password;

    @AssertTrue(message = "{msg.user-form.passwordDates.order}")
    public boolean isPasswordDatesOrderValid() {
        return lastPasswordChange == null || passwordExpiresAt == null || !passwordExpiresAt.isBefore(lastPasswordChange);
    }
}
