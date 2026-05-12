package org.iesalixar.daw2.womhat.womhat.services;

import org.iesalixar.daw2.womhat.womhat.dtos.UserRackAccessDTO;
import org.iesalixar.daw2.womhat.womhat.dtos.RackOptionDTO;
import org.iesalixar.daw2.womhat.womhat.enums.RackPermission;

import java.util.List;

/**
 * Servicio de negocio para la relación usuario-rack con permiso extra.
 */
public interface UserRackAccessService {

    /**
     * Lista accesos explícitos de un usuario sobre racks.
     */
    List<UserRackAccessDTO> listByUser(Long userId);

    /**
     * Lista accesos concedidos sobre un rack concreto.
     */
    List<UserRackAccessDTO> listByRack(Long rackId);

    /**
     * Concede o actualiza acceso por identificadores internos.
     */
    void grantOrUpdate(Long userId, Long rackId, RackPermission permission);

    /**
     * Concede o actualiza acceso resolviendo usuario por email.
     */
    void grantOrUpdateByEmail(String userEmail, Long rackId, RackPermission permission);

    /**
     * Concede o actualiza acceso registrando actor que delega.
     */
    void grantOrUpdateByEmail(String actorEmail, String userEmail, Long rackId, RackPermission permission);

    /**
     * Concede acceso de propietario funcional original (flujo de compra).
     */
    void grantOriginalOwnerByEmail(String userEmail, Long rackId);

    /**
     * Concede propietario original indicando actor que ejecuta la asignación.
     */
    void grantOriginalOwnerByEmail(String actorEmail, String userEmail, Long rackId);

    /**
     * Revoca un acceso por identificadores internos.
     */
    void revoke(Long userId, Long rackId);

    /**
     * Revoca un acceso registrando actor que ejecuta la operación.
     */
    void revoke(String actorEmail, Long userId, Long rackId);

    /**
     * Resuelve el permiso efectivo de un usuario sobre un rack.
     */
    RackPermission resolvePermission(String userEmail, Long rackId);

    /**
     * Indica si el usuario tiene administración global.
     */
    boolean hasGlobalAdminAccess(String userEmail);

    /**
     * Comprueba permiso de lectura sobre rack.
     */
    boolean canReadRack(String userEmail, Long rackId);

    /**
     * Comprueba permiso de escritura sobre rack.
     */
    boolean canWriteRack(String userEmail, Long rackId);

    /**
     * Comprueba permiso ADMIN sobre rack para delegar/revocar accesos.
     */
    boolean canManageRackAccess(String userEmail, Long rackId);

    /**
     * Indica si el usuario figura como propietario funcional original del rack.
     */
    boolean isOriginalOwner(String userEmail, Long rackId);

    /**
     * Devuelve el propietario funcional original si existe.
     */
    UserRackAccessDTO findOriginalOwner(Long rackId);

    /**
     * Lista racks donde el usuario puede operar con permiso WRITE o superior.
     */
    List<RackOptionDTO> listWritableRackOptions(String userEmail);
}
