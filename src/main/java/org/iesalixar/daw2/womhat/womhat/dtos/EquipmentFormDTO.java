package org.iesalixar.daw2.womhat.womhat.dtos;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.iesalixar.daw2.womhat.womhat.enums.EquipmentStatus;
import org.iesalixar.daw2.womhat.womhat.enums.EquipmentType;
import org.iesalixar.daw2.womhat.womhat.enums.NetworkConnectionType;
import org.iesalixar.daw2.womhat.womhat.enums.StorageType;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO de formulario para crear y editar equipos.
 *
 * Agrupa tanto los datos base como los campos
 * opcionales de las especializaciones 1:1.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentFormDTO {
    private static final String OPTIONAL_IP_REGEX =
            "^\\s*$|^((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}|(([0-9A-Fa-f]{1,4}:){2,7}[0-9A-Fa-f]{1,4}|::1|::))$";

    private static final String OPTIONAL_MAC_REGEX =
            "^\\s*$|^(([0-9A-Fa-f]{2}([:-][0-9A-Fa-f]{2}){5})|([0-9A-Fa-f]{4}(\\.[0-9A-Fa-f]{4}){2}))$";

    // -----------------------------
    // Datos base de Equipment
    // -----------------------------

    /** ID del equipo. En alta será null. */
    private Long id;

    /** Rack donde se instalará el equipo. */
    @NotNull(message = "{msg.equipment-form.rackId.notnull}")
    private Long rackId;

    /** Nombre del equipo. */
    @NotBlank(message = "{msg.equipment-form.name.notblank}")
    @Size(max = 120, message = "{msg.equipment-form.name.size}")
    private String name;

    /** Tipo del equipo. */
    @NotNull(message = "{msg.equipment-form.type.notnull}")
    private EquipmentType type;

    /** Número de serie único. */
    @NotBlank(message = "{msg.equipment-form.serialNumber.notblank}")
    @Size(max = 120, message = "{msg.equipment-form.serialNumber.size}")
    private String serialNumber;

    /** IP principal. */
    @Size(max = 45, message = "{msg.equipment-form.primaryIp.size}")
    @Pattern(regexp = OPTIONAL_IP_REGEX, message = "{msg.equipment-form.primaryIp.invalid}")
    private String primaryIp;

    /** IP de gestión. */
    @Size(max = 45, message = "{msg.equipment-form.managementIp.size}")
    @Pattern(regexp = OPTIONAL_IP_REGEX, message = "{msg.equipment-form.managementIp.invalid}")
    private String managementIp;

    /** VLAN asociada. */
    @Positive(message = "{msg.equipment-form.vlanId.positive}")
    private Integer vlanId;

    /** Dirección MAC. */
    @Size(max = 17, message = "{msg.equipment-form.macAddress.size}")
    @Pattern(regexp = OPTIONAL_MAC_REGEX, message = "{msg.equipment-form.macAddress.invalid}")
    private String macAddress;

    /** Posición inicial en U. */
    @Positive(message = "{msg.equipment-form.slotPositionU.positive}")
    private Integer slotPositionU;

    /** Altura ocupada en U. */
    @NotNull(message = "{msg.equipment-form.slotHeightU.notnull}")
    @Min(value = 1, message = "{msg.equipment-form.slotHeightU.min}")
    private Integer slotHeightU = 1;

    /** Estado actual del equipo. */
    @NotNull(message = "{msg.equipment-form.status.notnull}")
    private EquipmentStatus status = EquipmentStatus.ACTIVE;

    // -----------------------------
    // Datos opcionales de host_specifications
    // -----------------------------

    @Size(max = 120, message = "{msg.equipment-form.operatingSystem.size}")
    private String operatingSystem;

    @Size(max = 120, message = "{msg.equipment-form.purpose.size}")
    private String purpose;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate installDate;

    @Size(max = 80, message = "{msg.equipment-form.cpuArchitecture.size}")
    private String cpuArchitecture;

    @Size(max = 120, message = "{msg.equipment-form.cpuModel.size}")
    private String cpuModel;

    @PositiveOrZero(message = "{msg.equipment-form.cpuCores.positiveOrZero}")
    private Integer cpuCores;

    @PositiveOrZero(message = "{msg.equipment-form.cpuCacheMb.positiveOrZero}")
    private Integer cpuCacheMb;

    @Digits(integer = 4, fraction = 2, message = "{msg.equipment-form.cpuGhz.digits}")
    private BigDecimal cpuGhz;

    @Size(max = 60, message = "{msg.equipment-form.ramType.size}")
    private String ramType;

    @PositiveOrZero(message = "{msg.equipment-form.ramTotalGb.positiveOrZero}")
    private Integer ramTotalGb;

    @Digits(integer = 4, fraction = 2, message = "{msg.equipment-form.ramGhz.digits}")
    private BigDecimal ramGhz;

    @PositiveOrZero(message = "{msg.equipment-form.diskTotalGb.positiveOrZero}")
    private Integer diskTotalGb;

    @PositiveOrZero(message = "{msg.equipment-form.diskReadMbps.positiveOrZero}")
    private Integer diskReadMbps;

    @PositiveOrZero(message = "{msg.equipment-form.diskWriteMbps.positiveOrZero}")
    private Integer diskWriteMbps;

    @PositiveOrZero(message = "{msg.equipment-form.nicCount.positiveOrZero}")
    private Integer nicCount;

    @PositiveOrZero(message = "{msg.equipment-form.nicSpeedMbps.positiveOrZero}")
    private Integer nicSpeedMbps;

    @Size(max = 255, message = "{msg.equipment-form.hostNotes.size}")
    private String hostNotes;

    // -----------------------------
    // Datos opcionales de network_elements
    // -----------------------------

    private NetworkConnectionType connection = NetworkConnectionType.ETHERNET;

    @PositiveOrZero(message = "{msg.equipment-form.totalPorts.positiveOrZero}")
    private Integer totalPorts;

    // -----------------------------
    // Datos opcionales de storage_backups
    // -----------------------------

    @Size(max = 120, message = "{msg.equipment-form.distributionType.size}")
    private String distributionType;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate storageInstallDate;

    private StorageType storageType = StorageType.BACKUP;
}
