package org.iesalixar.daw2.womhat.womhat.services;

import org.iesalixar.daw2.womhat.womhat.dtos.PublicRegisterDTO;

/**
 * Servicio para el registro público desde la web.
 */
public interface PublicRegistrationService {

    /**
     * Registra un usuario nuevo con rol básico.
     *
     * @param dto datos del formulario público
     */
    void register(PublicRegisterDTO dto);
}