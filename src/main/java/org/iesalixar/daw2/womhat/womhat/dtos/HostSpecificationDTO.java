package org.iesalixar.daw2.womhat.womhat.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO de lectura para las especificaciones de host/servidor.
 *
 * Representa la información de host_specifications.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HostSpecificationDTO {

    /** ID del equipo asociado. */
    private Long equipmentId;

    /** Sistema operativo. */
    private String operatingSystem;

    /** Finalidad o propósito del host. */
    private String purpose;

    /** Fecha de instalación. */
    private LocalDate installDate;

    /** Arquitectura de CPU. */
    private String cpuArchitecture;

    /** Modelo de CPU. */
    private String cpuModel;

    /** Número de núcleos. */
    private Integer cpuCores;

    /** Caché de CPU en MB. */
    private Integer cpuCacheMb;

    /** Frecuencia de CPU en GHz. */
    private BigDecimal cpuGhz;

    /** Tipo de memoria RAM. */
    private String ramType;

    /** RAM total en GB. */
    private Integer ramTotalGb;

    /** Frecuencia de RAM en GHz. */
    private BigDecimal ramGhz;

    /** Disco total en GB. */
    private Integer diskTotalGb;

    /** Velocidad de lectura en MB/s. */
    private Integer diskReadMbps;

    /** Velocidad de escritura en MB/s. */
    private Integer diskWriteMbps;

    /** Número de interfaces de red. */
    private Integer nicCount;

    /** Velocidad de red en Mbps. */
    private Integer nicSpeedMbps;

    /** Notas adicionales. */
    private String notes;
}