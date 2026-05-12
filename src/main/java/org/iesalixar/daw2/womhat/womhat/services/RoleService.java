package org.iesalixar.daw2.womhat.womhat.services;

import org.iesalixar.daw2.womhat.womhat.dtos.RoleDTO;

import java.util.List;

/**
 * Servicio de apoyo para roles del sistema.
 */
public interface RoleService {

    List<RoleDTO> listAll();

    RoleDTO getByName(String name);
}