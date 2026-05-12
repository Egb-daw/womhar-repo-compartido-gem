package org.iesalixar.daw2.womhat.womhat.repositories;

import org.iesalixar.daw2.womhat.womhat.entities.UserRackAccess;
import org.iesalixar.daw2.womhat.womhat.entities.UserRackAccessId;
import org.iesalixar.daw2.womhat.womhat.enums.RackPermission;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data JPA para la entidad {@link UserRackAccess}.
 *
 * <p>Esta entidad modela la relación entre usuario y rack con un permiso extra,
 * por eso se trabaja con clave primaria compuesta ({@link UserRackAccessId}).</p>
 */
public interface UserRackAccessRepository extends JpaRepository<UserRackAccess, UserRackAccessId> {

    /**
     * Busca el permiso concreto de un usuario sobre un rack.
     *
     * @param userId id del usuario
     * @param rackId id del rack
     * @return acceso si existe
     */
    Optional<UserRackAccess> findByIdUserIdAndIdRackId(Long userId, Long rackId);

    /**
     * Busca al propietario funcional original de un rack, si está marcado.
     *
     * @param rackId id del rack
     * @return acceso del propietario original si existe
     */
    @EntityGraph(attributePaths = {"user", "rack", "grantedBy"})
    Optional<UserRackAccess> findFirstByIdRackIdAndOriginalOwnerTrue(Long rackId);

    /**
     * Comprueba si un usuario tiene alguna entrada de acceso sobre un rack.
     *
     * @param userId id del usuario
     * @param rackId id del rack
     * @return true si existe
     */
    boolean existsByIdUserIdAndIdRackId(Long userId, Long rackId);

    /**
     * Recupera todos los accesos de un usuario.
     *
     * @param userId id del usuario
     * @return lista de accesos
     */
    List<UserRackAccess> findByIdUserId(Long userId);

    /**
     * Recupera todos los accesos definidos sobre un rack.
     *
     * @param rackId id del rack
     * @return lista de accesos
     */
    List<UserRackAccess> findByIdRackId(Long rackId);

    /**
     * Recupera accesos filtrados por permiso.
     *
     * @param permission permiso
     * @return lista filtrada
     */
    List<UserRackAccess> findByPermission(RackPermission permission);

    /**
     * Recupera todos los accesos de un usuario cargando también las entidades user y rack.
     *
     * @param userId id del usuario
     * @return lista de accesos detallados
     */
    @EntityGraph(attributePaths = {"user", "rack", "grantedBy"})
    List<UserRackAccess> findDetailedByIdUserId(Long userId);

    /**
     * Recupera todos los accesos de un rack cargando también las entidades user y rack.
     *
     * @param rackId id del rack
     * @return lista de accesos detallados
     */
    @EntityGraph(attributePaths = {"user", "rack", "grantedBy"})
    List<UserRackAccess> findDetailedByIdRackId(Long rackId);
}
