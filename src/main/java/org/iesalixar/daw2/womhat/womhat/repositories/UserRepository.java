package org.iesalixar.daw2.womhat.womhat.repositories;

import org.iesalixar.daw2.womhat.womhat.entities.User;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Repositorio Spring Data JPA para la entidad {@link User}.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Comprueba si existe un usuario con el email indicado.
     *
     * @param email email del usuario
     * @return true si existe
     */
    boolean existsByEmail(String email);

    /**
     * Comprueba si existe un usuario con el email indicado ignorando mayúsculas/minúsculas.
     *
     * @param email email del usuario
     * @return true si existe
     */
    boolean existsByEmailIgnoreCase(String email);

    /**
     * Comprueba si existe un usuario con ese email excluyendo un id concreto.
     *
     * @param email email a comprobar
     * @param id id a excluir
     * @return true si existe otro usuario con ese email
     */
    boolean existsByEmailAndIdNot(String email, Long id);

    /**
     * Busca un usuario por id.
     *
     * @param id identificador
     * @return usuario si existe
     */
    @Override
    Optional<User> findById(Long id);

    /**
     * Busca un usuario por email cargando sus roles en la misma consulta.
     *
     * @param email email del usuario
     * @return usuario con roles si existe
     */
    @EntityGraph(attributePaths = "roles")
    Optional<User> findByEmail(String email);

    /**
     * Busca un usuario por email ignorando mayúsculas/minúsculas y cargando sus roles.
     *
     * @param email email del usuario
     * @return usuario con roles si existe
     */
    @EntityGraph(attributePaths = "roles")
    Optional<User> findByEmailIgnoreCase(String email);

    /**
     * Cuenta cuántos usuarios tienen un rol concreto.
     *
     * @param roleName nombre interno del rol (ej. ROLE_ADMIN)
     * @return número de usuarios con ese rol
     */
    long countByRoles_Name(String roleName);

    /**
     * Asegura por SQL que exista la relación user_roles para el usuario/rol indicado.
     * INSERT IGNORE evita error si ya existe.
     *
     * @param userId id de usuario
     * @param roleId id del rol
     * @return filas afectadas (1 si inserta, 0 si ya existía)
     */
    @Modifying
    @Query(value = "INSERT IGNORE INTO user_roles (user_id, role_id) VALUES (:userId, :roleId)", nativeQuery = true)
    int ensureUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);
}
