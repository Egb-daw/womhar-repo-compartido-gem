package org.iesalixar.daw2.womhat.womhat.services;

import org.iesalixar.daw2.womhat.womhat.dtos.UserCreateDTO;
import org.iesalixar.daw2.womhat.womhat.dtos.UserDTO;
import org.iesalixar.daw2.womhat.womhat.dtos.UserDetailDTO;
import org.iesalixar.daw2.womhat.womhat.dtos.UserUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Servicio de negocio del CRUD de usuarios.
 */
public interface UserService {

    /**
     * Lista paginada de usuarios.
     */
    Page<UserDTO> list(Pageable pageable);

    /**
     * Recupera un usuario para cargar el formulario de edición.
     */
    UserUpdateDTO getForEdit(Long id);

    /**
     * Crea un usuario nuevo.
     */
    void create(UserCreateDTO dto);

    /**
     * Actualiza un usuario existente.
     */
    void update(UserUpdateDTO dto);

    /**
     * Elimina un usuario.
     */
    void delete(Long id);

    /**
     * Recupera el detalle de un usuario.
     */
    UserDetailDTO getDetail(Long id);
}