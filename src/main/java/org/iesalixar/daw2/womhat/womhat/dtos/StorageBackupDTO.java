package org.iesalixar.daw2.womhat.womhat.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.iesalixar.daw2.womhat.womhat.enums.StorageType;

import java.time.LocalDate;

/**
 * DTO de lectura para storage_backups.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StorageBackupDTO {

    /** ID del equipo asociado. */
    private Long equipmentId;

    /** Tipo de distribución del almacenamiento. */
    private String distributionType;

    /** Fecha de instalación. */
    private LocalDate installDate;

    /** Tipo de almacenamiento. */
    private StorageType storageType;
}