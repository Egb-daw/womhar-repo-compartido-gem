package org.iesalixar.daw2.womhat.womhat.repositories;

import org.iesalixar.daw2.womhat.womhat.entities.StorageBackup;
import org.iesalixar.daw2.womhat.womhat.enums.StorageType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data JPA para la entidad {@link StorageBackup}.
 *
 * <p>Gestiona la información extendida de equipos de almacenamiento/backup.</p>
 */
public interface StorageBackupRepository extends JpaRepository<StorageBackup, Long> {

    /**
     * Recupera la información de storage asociada a un equipo.
     *
     * @param equipmentId id del equipo
     * @return storage si existe
     */
    Optional<StorageBackup> findByEquipment_Id(Long equipmentId);

    /**
     * Comprueba si existe información de storage para un equipo.
     *
     * @param equipmentId id del equipo
     * @return true si existe
     */
    boolean existsByEquipment_Id(Long equipmentId);

    /**
     * Lista los registros filtrados por tipo de almacenamiento.
     *
     * @param storageType tipo de almacenamiento
     * @return lista filtrada
     */
    List<StorageBackup> findByStorageType(StorageType storageType);

    /**
     * Recupera el storage cargando también el equipo asociado.
     *
     * @param equipmentId id del equipo
     * @return storage detallado si existe
     */
    @EntityGraph(attributePaths = "equipment")
    Optional<StorageBackup> findDetailedByEquipment_Id(Long equipmentId);
}