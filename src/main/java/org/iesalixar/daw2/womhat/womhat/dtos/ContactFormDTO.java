package org.iesalixar.daw2.womhat.womhat.dtos;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO del formulario público de contacto.
 */
@Data
public class ContactFormDTO {

    /** Correo electrónico del contacto. */
    @NotBlank(message = "{msg.public.contact.email.notblank}")
    @Email(message = "{msg.public.contact.email.invalid}")
    @Size(max = 120, message = "{msg.public.contact.email.size}")
    private String email;

    /** Nombre del contacto. */
    @NotBlank(message = "{msg.public.contact.name.notblank}")
    @Size(max = 100, message = "{msg.public.contact.name.size}")
    private String nombre;

    /** Teléfono de contacto (opcional). */
    @Size(max = 30, message = "{msg.public.contact.phone.size}")
    @Pattern(
            regexp = "^[0-9+()\\s-]*$",
            message = "{msg.public.contact.phone.invalid}"
    )
    private String telefono;

    /** Motivo del contacto. */
    @NotBlank(message = "{msg.public.contact.reason.notblank}")
    @Size(max = 40, message = "{msg.public.contact.reason.size}")
    private String motivo = "info";

    /** Mensaje del contacto. */
    @NotBlank(message = "{msg.public.contact.message.notblank}")
    @Size(min = 10, max = 1000, message = "{msg.public.contact.message.size}")
    private String mensaje;

    /** Preferencia de contacto (email, phone, etc.). */
    @Size(max = 20, message = "{msg.public.contact.preference.size}")
    private String pref = "email";

    /** Aceptación de términos y condiciones. */
    @AssertTrue(message = "{msg.public.contact.terms.required}")
    private boolean terms;
}