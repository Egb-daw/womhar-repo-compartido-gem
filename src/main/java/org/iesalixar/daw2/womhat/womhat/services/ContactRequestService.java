package org.iesalixar.daw2.womhat.womhat.services;

import org.iesalixar.daw2.womhat.womhat.dtos.ContactFormDTO;

import java.util.Locale;

/**
 * Servicio para el envío del formulario de contacto.
 */
public interface ContactRequestService {

    /**
     * Envía el mensaje de contacto al buzón configurado.
     *
     * @param dto datos del formulario
     * @param locale idioma actual
     */
    boolean sendContactMessage(ContactFormDTO dto, Locale locale);
}
