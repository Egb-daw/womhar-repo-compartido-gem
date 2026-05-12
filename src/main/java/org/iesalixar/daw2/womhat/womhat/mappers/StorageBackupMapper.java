package org.iesalixar.daw2.womhat.womhat.mappers;

import org.iesalixar.daw2.womhat.womhat.dtos.EquipmentFormDTO;
import org.iesalixar.daw2.womhat.womhat.dtos.StorageBackupDTO;
import org.iesalixar.daw2.womhat.womhat.entities.Equipment;
import org.iesalixar.daw2.womhat.womhat.entities.StorageBackup;

/**
 * Mapper para la especialización 1:1 {@link StorageBackup}.
 */
public class StorageBackupMapper {

    /**
     * Convierte la entidad storage a DTO de lectura.
     */
    public static StorageBackupDTO toDTO(StorageBackup entity) {
        if (entity == null) {
            return null;
        }

        return new StorageBackupDTO(
                entity.getId(),
                entity.getDistributionType(),
                entity.getInstallDate(),
                entity.getStorageType()
        );
    }

    /**
     * Copia los campos de storage al formulario combinado del equipo.
     */
    public static void copyToFormDTO(StorageBackup entity, EquipmentFormDTO dto) {
        if (entity == null || dto == null) {
            return;
        }

        dto.setDistributionType(entity.getDistributionType());
        dto.setStorageInstallDate(entity.getInstallDate());
        dto.setStorageType(entity.getStorageType());
    }

    /**
     * Crea una nueva entidad 1:1 a partir del formulario y del equipo padre.
     */
    public static StorageBackup toEntity(EquipmentFormDTO dto, Equipment equipment) {
        if (dto == null || equipment == null) {
            return null;
        }

        StorageBackup entity = new StorageBackup();
        entity.setEquipment(equipment);
        entity.setDistributionType(dto.getDistributionType());
        entity.setInstallDate(dto.getStorageInstallDate());
        entity.setStorageType(dto.getStorageType());
        return entity;
    }

    /**
     * Copia los campos de storage del formulario sobre una entidad existente.
     */
    public static void copyToExistingEntity(EquipmentFormDTO dto, StorageBackup entity) {
        if (dto == null || entity == null) {
            return;
        }

        entity.setDistributionType(dto.getDistributionType());
        entity.setInstallDate(dto.getStorageInstallDate());
        entity.setStorageType(dto.getStorageType());
    }
}