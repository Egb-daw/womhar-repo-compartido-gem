package org.iesalixar.daw2.womhat.womhat.repositories;

import org.iesalixar.daw2.womhat.womhat.entities.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Repository para operaciones CRUD sobre la entidad {@link UserProfile}.
 *
 * <p>Pensado para la funcionalidad de gestión de perfil de usuario.</p>
 */
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    /**
     * Comprueba si existe un perfil asociado al usuario indicado.
     *
     * @param userId id del usuario
     * @return true si existe
     */
    boolean existsByUserId(Long userId);

    /**
     * Busca un perfil por id.
     *
     * @param id identificador
     * @return perfil si existe
     */
    @Override
    Optional<UserProfile> findById(Long id);

    /**
     * Recupera el perfil asociado a un usuario.
     *
     * @param userId id del usuario
     * @return perfil si existe
     */
    Optional<UserProfile> findByUserId(Long userId);

    /**
     * Recupera un perfil cargando también su usuario asociado.
     *
     * @param userId id del usuario
     * @return perfil detallado si existe
     */
    @Query("select up from UserProfile up join fetch up.user where up.user.id = :userId")
    Optional<UserProfile> findByUserIdWithUser(@Param("userId") Long userId);
}