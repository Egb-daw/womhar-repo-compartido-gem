package org.iesalixar.daw2.womhat.womhat.repositories;

import org.iesalixar.daw2.womhat.womhat.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data JPA para la entidad {@link Role}.
 */
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Obtiene el listado completo de roles.
     *
     * @return lista de roles
     */
    @Override
    List<Role> findAll();

    /**
     * Recupera todos los roles cuyos ids están en el conjunto indicado.
     *
     * @param ids ids de roles
     * @return lista de roles
     */
    @Override
    List<Role> findAllById(Iterable<Long> ids);

    /**
     * Busca un rol por id.
     *
     * @param id identificador
     * @return rol si existe
     */
    @Override
    Optional<Role> findById(Long id);

    /**
     * Busca un rol por nombre exacto.
     *
     * @param name nombre del rol
     * @return rol si existe
     */
    Optional<Role> findByName(String name);

    /**
     * Busca un rol por nombre ignorando mayúsculas/minúsculas.
     *
     * @param name nombre del rol
     * @return rol si existe
     */
    Optional<Role> findByNameIgnoreCase(String name);
}