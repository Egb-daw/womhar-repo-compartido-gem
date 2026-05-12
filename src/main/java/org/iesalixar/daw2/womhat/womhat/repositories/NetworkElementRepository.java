package org.iesalixar.daw2.womhat.womhat.repositories;

import org.iesalixar.daw2.womhat.womhat.entities.NetworkElement;
import org.iesalixar.daw2.womhat.womhat.enums.NetworkConnectionType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data JPA para la entidad {@link NetworkElement}.
 *
 * <p>Gestiona la información extendida de equipos de red.</p>
 */
public interface NetworkElementRepository extends JpaRepository<NetworkElement, Long> {

    /**
     * Recupera el elemento de red asociado a un equipo.
     *
     * @param equipmentId id del equipo
     * @return elemento de red si existe
     */
    Optional<NetworkElement> findByEquipment_Id(Long equipmentId);

    /**
     * Comprueba si existe ficha de red para un equipo.
     *
     * @param equipmentId id del equipo
     * @return true si existe
     */
    boolean existsByEquipment_Id(Long equipmentId);

    /**
     * Lista elementos de red por tipo de conexión.
     *
     * @param connection tipo de conexión
     * @return lista filtrada
     */
    List<NetworkElement> findByConnection(NetworkConnectionType connection);

    /**
     * Recupera el elemento de red cargando también el equipo asociado.
     *
     * @param equipmentId id del equipo
     * @return elemento detallado si existe
     */
    @EntityGraph(attributePaths = "equipment")
    Optional<NetworkElement> findDetailedByEquipment_Id(Long equipmentId);
}