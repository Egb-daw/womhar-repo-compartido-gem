package org.iesalixar.daw2.womhat.womhat.repositories;

import org.iesalixar.daw2.womhat.womhat.entities.DataCenter;
import org.iesalixar.daw2.womhat.womhat.enums.DataCenterStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data JPA para la entidad {@link DataCenter}.
 *
 * <p>Permite recuperar CPDs por código, estado y cargar sus salas cuando sea necesario.</p>
 */
public interface DataCenterRepository extends JpaRepository<DataCenter, Long> {

    /**
     * Busca un CPD por su código.
     *
     * @param code código único del CPD
     * @return CPD si existe
     */
    Optional<DataCenter> findByCode(String code);

    /**
     * Busca un CPD por su código ignorando mayúsculas/minúsculas.
     *
     * @param code código del CPD
     * @return CPD si existe
     */
    Optional<DataCenter> findByCodeIgnoreCase(String code);

    /**
     * Comprueba si ya existe un CPD con el código indicado.
     *
     * @param code código a comprobar
     * @return true si existe
     */
    boolean existsByCode(String code);

    /**
     * Lista los CPDs filtrados por estado.
     *
     * @param status estado del CPD
     * @return lista de CPDs
     */
    List<DataCenter> findByStatus(DataCenterStatus status);

    /**
     * Recupera un CPD por id cargando también sus salas.
     *
     * @param id id del CPD
     * @return CPD con la colección rooms inicializada si existe
     */
    @EntityGraph(attributePaths = "rooms")
    Optional<DataCenter> findWithRoomsById(Long id);
}