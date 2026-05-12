package org.iesalixar.daw2.womhat.womhat.repositories;

import org.iesalixar.daw2.womhat.womhat.entities.DataCenterRoom;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data JPA para la entidad {@link DataCenterRoom}.
 *
 * <p>Permite gestionar salas de CPD y sus búsquedas por CPD asociado.</p>
 */
public interface DataCenterRoomRepository extends JpaRepository<DataCenterRoom, Long> {

    /**
     * Recupera todas las salas de un CPD.
     *
     * @param dataCenterId id del CPD
     * @return lista de salas del CPD
     */
    List<DataCenterRoom> findByDataCenter_Id(Long dataCenterId);

    /**
     * Recupera todas las salas de un CPD ordenadas por nombre.
     *
     * @param dataCenterId id del CPD
     * @return lista ordenada de salas
     */
    List<DataCenterRoom> findByDataCenter_IdOrderByNameAsc(Long dataCenterId);

    /**
     * Busca una sala concreta dentro de un CPD por nombre.
     *
     * @param dataCenterId id del CPD
     * @param name nombre de la sala
     * @return sala si existe
     */
    Optional<DataCenterRoom> findByDataCenter_IdAndName(Long dataCenterId, String name);

    /**
     * Comprueba si ya existe una sala con ese nombre dentro de un CPD.
     *
     * @param dataCenterId id del CPD
     * @param name nombre de la sala
     * @return true si existe
     */
    boolean existsByDataCenter_IdAndName(Long dataCenterId, String name);

    /**
     * Recupera una sala por id cargando también su CPD y sus racks.
     *
     * @param id id de la sala
     * @return sala con relaciones inicializadas si existe
     */
    @EntityGraph(attributePaths = {"dataCenter", "racks"})
    Optional<DataCenterRoom> findDetailedById(Long id);
}