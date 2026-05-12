package org.iesalixar.daw2.womhat.womhat.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO del formulario público de registro.
 */
@Data
public class PublicRegisterDTO {

    /**
     * Campo visual del formulario.
     * En el modelo actual de usuarios no existe "username" como columna,
     * por eso lo reutilizamos para crear el perfil básico del usuario.
     */
    @NotBlank(message = "{msg.public.register.username.notblank}")
    @Size(max = 60, message = "{msg.public.register.username.size}")
    private String username;

    /**
     * Email real de acceso.
     * El login usa el email como identificador.
     */
    @NotBlank(message = "{msg.public.register.email.notblank}")
    @Email(message = "{msg.public.register.email.invalid}")
    @Size(max = 100, message = "{msg.public.register.email.size}")
    private String email;

    /**
     * Contraseña en claro enviada desde el formulario.
     * Se codificará con BCrypt antes de persistirla.
     */
    @NotBlank(message = "{msg.public.register.password.notblank}")
    @Size(min = 8, max = 72, message = "{msg.public.register.password.size}")
    private String password;

    /**
     * Campo de confirmación de contraseña para validar que el usuario ha escrito la misma contraseña dos veces.
     */
    @NotBlank(message = "{msg.public.register.confirmPassword.notblank}")
    @Size(min = 8, max = 72, message = "{msg.public.register.password.size}")
    private String confirmPassword;
}