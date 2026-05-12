package org.iesalixar.daw2.womhat.womhat.mappers;

import org.iesalixar.daw2.womhat.womhat.dtos.EquipmentFormDTO;
import org.iesalixar.daw2.womhat.womhat.dtos.HostSpecificationDTO;
import org.iesalixar.daw2.womhat.womhat.entities.Equipment;
import org.iesalixar.daw2.womhat.womhat.entities.HostSpecification;

/**
 * Mapper para la especialización 1:1 {@link HostSpecification}.
 */
public class HostSpecificationMapper {

    /**
     * Convierte la entidad de host a DTO de lectura.
     */
    public static HostSpecificationDTO toDTO(HostSpecification entity) {
        if (entity == null) {
            return null;
        }

        return new HostSpecificationDTO(
                entity.getId(),
                entity.getOperatingSystem(),
                entity.getPurpose(),
                entity.getInstallDate(),
                entity.getCpuArchitecture(),
                entity.getCpuModel(),
                entity.getCpuCores(),
                entity.getCpuCacheMb(),
                entity.getCpuGhz(),
                entity.getRamType(),
                entity.getRamTotalGb(),
                entity.getRamGhz(),
                entity.getDiskTotalGb(),
                entity.getDiskReadMbps(),
                entity.getDiskWriteMbps(),
                entity.getNicCount(),
                entity.getNicSpeedMbps(),
                entity.getNotes()
        );
    }

    /**
     * Copia los campos de host sobre el DTO de formulario del equipo.
     */
    public static void copyToFormDTO(HostSpecification entity, EquipmentFormDTO dto) {
        if (entity == null || dto == null) {
            return;
        }

        dto.setOperatingSystem(entity.getOperatingSystem());
        dto.setPurpose(entity.getPurpose());
        dto.setInstallDate(entity.getInstallDate());
        dto.setCpuArchitecture(entity.getCpuArchitecture());
        dto.setCpuModel(entity.getCpuModel());
        dto.setCpuCores(entity.getCpuCores());
        dto.setCpuCacheMb(entity.getCpuCacheMb());
        dto.setCpuGhz(entity.getCpuGhz());
        dto.setRamType(entity.getRamType());
        dto.setRamTotalGb(entity.getRamTotalGb());
        dto.setRamGhz(entity.getRamGhz());
        dto.setDiskTotalGb(entity.getDiskTotalGb());
        dto.setDiskReadMbps(entity.getDiskReadMbps());
        dto.setDiskWriteMbps(entity.getDiskWriteMbps());
        dto.setNicCount(entity.getNicCount());
        dto.setNicSpeedMbps(entity.getNicSpeedMbps());
        dto.setHostNotes(entity.getNotes());
    }

    /**
     * Crea una nueva entidad 1:1 a partir del formulario y del equipo padre.
     */
    public static HostSpecification toEntity(EquipmentFormDTO dto, Equipment equipment) {
        if (dto == null || equipment == null) {
            return null;
        }

        HostSpecification entity = new HostSpecification();
        entity.setEquipment(equipment);
        entity.setOperatingSystem(dto.getOperatingSystem());
        entity.setPurpose(dto.getPurpose());
        entity.setInstallDate(dto.getInstallDate());
        entity.setCpuArchitecture(dto.getCpuArchitecture());
        entity.setCpuModel(dto.getCpuModel());
        entity.setCpuCores(dto.getCpuCores());
        entity.setCpuCacheMb(dto.getCpuCacheMb());
        entity.setCpuGhz(dto.getCpuGhz());
        entity.setRamType(dto.getRamType());
        entity.setRamTotalGb(dto.getRamTotalGb());
        entity.setRamGhz(dto.getRamGhz());
        entity.setDiskTotalGb(dto.getDiskTotalGb());
        entity.setDiskReadMbps(dto.getDiskReadMbps());
        entity.setDiskWriteMbps(dto.getDiskWriteMbps());
        entity.setNicCount(dto.getNicCount());
        entity.setNicSpeedMbps(dto.getNicSpeedMbps());
        entity.setNotes(dto.getHostNotes());
        return entity;
    }

    /**
     * Copia los datos de host del formulario sobre una entidad existente.
     */
    public static void copyToExistingEntity(EquipmentFormDTO dto, HostSpecification entity) {
        if (dto == null || entity == null) {
            return;
        }

        entity.setOperatingSystem(dto.getOperatingSystem());
        entity.setPurpose(dto.getPurpose());
        entity.setInstallDate(dto.getInstallDate());
        entity.setCpuArchitecture(dto.getCpuArchitecture());
        entity.setCpuModel(dto.getCpuModel());
        entity.setCpuCores(dto.getCpuCores());
        entity.setCpuCacheMb(dto.getCpuCacheMb());
        entity.setCpuGhz(dto.getCpuGhz());
        entity.setRamType(dto.getRamType());
        entity.setRamTotalGb(dto.getRamTotalGb());
        entity.setRamGhz(dto.getRamGhz());
        entity.setDiskTotalGb(dto.getDiskTotalGb());
        entity.setDiskReadMbps(dto.getDiskReadMbps());
        entity.setDiskWriteMbps(dto.getDiskWriteMbps());
        entity.setNicCount(dto.getNicCount());
        entity.setNicSpeedMbps(dto.getNicSpeedMbps());
        entity.setNotes(dto.getHostNotes());
    }
}