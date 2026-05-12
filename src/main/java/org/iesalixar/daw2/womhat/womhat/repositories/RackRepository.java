package org.iesalixar.daw2.womhat.womhat.repositories;

import org.iesalixar.daw2.womhat.womhat.entities.Rack;
import org.iesalixar.daw2.womhat.womhat.enums.RackStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data JPA para la entidad {@link Rack}.
 *
 * <p>Permite localizar racks por sala, etiqueta de ubicación, estado y grupo.</p>
 */
public interface RackRepository extends JpaRepository<Rack, Long> {

    /**
     * Busca un rack por su etiqueta de localización.
     *
     * @param locationLabel etiqueta única de localización
     * @return rack si existe
     */
    Optional<Rack> findByLocationLabel(String locationLabel);

    /**
     * Busca un rack por su etiqueta de localización ignorando mayúsculas/minúsculas.
     *
     * @param locationLabel etiqueta del rack
     * @return rack si existe
     */
    Optional<Rack> findByLocationLabelIgnoreCase(String locationLabel);

    /**
     * Comprueba si ya existe un rack con esa etiqueta.
     *
     * @param locationLabel etiqueta del rack
     * @return true si existe
     */
    boolean existsByLocationLabel(String locationLabel);

    /**
     * Lista los racks de una sala concreta.
     *
     * @param roomId id de la sala
     * @return lista de racks
     */
    List<Rack> findByRoom_Id(Long roomId);

    /**
     * Lista los racks de una sala ordenados por etiqueta de localización.
     *
     * @param roomId id de la sala
     * @return lista ordenada de racks
     */
    List<Rack> findByRoom_IdOrderByLocationLabelAsc(Long roomId);

    /**
     * Lista racks por estado.
     *
     * @param status estado del rack
     * @return lista filtrada
     */
    List<Rack> findByStatus(RackStatus status);

    /**
     * Lista racks por grupo funcional.
     *
     * @param groupName nombre del grupo
     * @return lista filtrada
     */
    List<Rack> findByGroupNameIgnoreCase(String groupName);

    /**
     * Recupera un rack por id cargando sala y equipos.
     *
     * @param id id del rack
     * @return rack detallado si existe
     */
    @EntityGraph(attributePaths = {"room", "equipments"})
    Optional<Rack> findDetailedById(Long id);
}