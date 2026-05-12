package org.iesalixar.daw2.womhat.womhat.repositories;

import org.iesalixar.daw2.womhat.womhat.entities.HostSpecification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositorio Spring Data JPA para la entidad {@link HostSpecification}.
 *
 * <p>Gestiona la información extendida de equipos tipo servidor/host.</p>
 */
public interface HostSpecificationRepository extends JpaRepository<HostSpecification, Long> {

    /**
     * Recupera la especificación de host asociada a un equipo.
     *
     * @param equipmentId id del equipo
     * @return especificación si existe
     */
    Optional<HostSpecification> findByEquipment_Id(Long equipmentId);

    /**
     * Comprueba si existe especificación de host para un equipo.
     *
     * @param equipmentId id del equipo
     * @return true si existe
     */
    boolean existsByEquipment_Id(Long equipmentId);

    /**
     * Recupera la especificación cargando también el equipo asociado.
     *
     * @param equipmentId id del equipo
     * @return especificación detallada si existe
     */
    @EntityGraph(attributePaths = "equipment")
    Optional<HostSpecification> findDetailedByEquipment_Id(Long equipmentId);
}