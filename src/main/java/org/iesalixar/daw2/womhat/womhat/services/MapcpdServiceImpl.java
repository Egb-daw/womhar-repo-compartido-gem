package org.iesalixar.daw2.womhat.womhat.services;

import org.iesalixar.daw2.womhat.womhat.dtos.EquipmentDTO;
import org.iesalixar.daw2.womhat.womhat.dtos.RackDTO;
import org.iesalixar.daw2.womhat.womhat.entities.Equipment;
import org.iesalixar.daw2.womhat.womhat.entities.Rack;
import org.iesalixar.daw2.womhat.womhat.repositories.EquipmentRepository;
import org.iesalixar.daw2.womhat.womhat.repositories.RackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MapcpdServiceImpl implements MapcpdService {

    @Autowired
    private RackRepository rackRepository;

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Override
    @Transactional(readOnly = true)
    public List<RackDTO> getMapData() {
        // Usamos findAll() y luego cargamos manualmente los equipos con EntityGraph
        // O mejor, creamos un método en el repo para obtener todos con equipos
        // Por ahora, usamos un truco: buscar todos y luego cargar equipos uno a uno (ineficiente)
        // O mejor aún: crear un método en el repo con @EntityGraph para findAll

        // Solución óptima: Añadir un método en RackRepository con @EntityGraph para findAll
        // Si no quieres modificar el repo, podemos usar un enfoque diferente:
        // Obtener todos los racks y luego, para cada uno, cargar sus equipos con findDetailedById

        List<Rack> allRacks = rackRepository.findAll();
        List<RackDTO> result = new ArrayList<>();

        for (Rack rack : allRacks) {
            // Cargar equipos con EntityGraph
            Rack detailedRack = rackRepository.findDetailedById(rack.getId())
                    .orElse(rack);

            result.add(convertToRackDTO(detailedRack));
        }

        return result;
    }

    @Override
    @Transactional
    public RackDTO saveRackWithEquipment(RackDTO rackDTO) {
        Rack rack;
        if (rackDTO.getId() != null) {
            rack = rackRepository.findById(rackDTO.getId())
                    .orElseThrow(() -> new RuntimeException("Rack no encontrado"));
            actualizarRackFromDTO(rack, rackDTO);
        } else {
            rack = new Rack();
            // Asignar campos básicos
            actualizarRackFromDTO(rack, rackDTO);
            // Asignar room si es necesario (puede venir en el DTO o ser por defecto)
            // rack.setRoom(new DataCenterRoom()); // Si necesitas crear una sala por defecto
        }

        // Gestionar equipos
        // Opción 1: Borrar todos los equipos existentes y añadir los nuevos del DTO
        // Opción 2: Actualizar existentes y añadir nuevos
        // Para simplificar, usaremos Opción 1 (reemplazo total)
        rack.getEquipments().clear();

        if (rackDTO.getEquipments() != null) {
            for (EquipmentDTO eqDTO : rackDTO.getEquipments()) {
                Equipment eq;
                if (eqDTO.getId() != null) {
                    eq = equipmentRepository.findById(eqDTO.getId())
                            .orElse(new Equipment());
                    actualizarEquipmentFromDTO(eq, eqDTO);
                } else {
                    eq = new Equipment();
                    crearEquipmentFromDTO(eq, eqDTO);
                }
                eq.setRack(rack);
                rack.getEquipments().add(eq);
            }
        }

        Rack savedRack = rackRepository.save(rack);
        return convertToRackDTO(savedRack);
    }

    @Override
    @Transactional
    public void deleteRack(Long id) {
        if (!rackRepository.existsById(id)) {
            throw new RuntimeException("Rack no encontrado");
        }
        rackRepository.deleteById(id);
    }

    // --- Métodos auxiliares de mapeo ---

    private void actualizarRackFromDTO(Rack rack, RackDTO dto) {
        rack.setLocationLabel(dto.getLocationLabel());
        rack.setCapacityU(dto.getCapacityU());
        rack.setFunctionName(dto.getFunctionName());
        rack.setGroupName(dto.getGroupName());
        rack.setDimension(dto.getDimension());
        rack.setPositionX(dto.getPositionX());
        rack.setPositionY(dto.getPositionY());
        rack.setStatus(dto.getStatus());
        rack.setCatalogVisible(dto.isCatalogVisible());
        rack.setCatalogPrice(dto.getCatalogPrice());
        rack.setCatalogStock(dto.getCatalogStock());
        rack.setCatalogSummary(dto.getCatalogSummary());
        // No actualizamos room aquí, asumimos que ya tiene una o se asigna aparte
    }

    private void crearEquipmentFromDTO(Equipment eq, EquipmentDTO dto) {
        eq.setName(dto.getName());
        eq.setType(dto.getType());
        eq.setSerialNumber(dto.getSerialNumber());
        eq.setPrimaryIp(dto.getPrimaryIp());
        eq.setManagementIp(dto.getManagementIp());
        eq.setVlanId(dto.getVlanId());
        eq.setMacAddress(dto.getMacAddress());
        eq.setSlotPositionU(dto.getSlotPositionU());
        eq.setSlotHeightU(dto.getSlotHeightU());
        eq.setStatus(dto.getStatus());
    }

    private void actualizarEquipmentFromDTO(Equipment eq, EquipmentDTO dto) {
        crearEquipmentFromDTO(eq, dto);
    }

    private RackDTO convertToRackDTO(Rack rack) {
        RackDTO dto = new RackDTO();
        dto.setId(rack.getId());
        dto.setLocationLabel(rack.getLocationLabel());
        dto.setCapacityU(rack.getCapacityU());
        dto.setFunctionName(rack.getFunctionName());
        dto.setGroupName(rack.getGroupName());
        dto.setDimension(rack.getDimension());
        dto.setPositionX(rack.getPositionX());
        dto.setPositionY(rack.getPositionY());
        dto.setStatus(rack.getStatus());
        dto.setCatalogVisible(rack.isCatalogVisible());
        dto.setCatalogPrice(rack.getCatalogPrice());
        dto.setCatalogStock(rack.getCatalogStock());
        dto.setCatalogSummary(rack.getCatalogSummary());
        // Mapear room si es necesario
        if (rack.getRoom() != null) {
            dto.setRoomId(rack.getRoom().getId());
            dto.setRoomName(rack.getRoom().getName());
        }

        // Mapear equipos
        List<EquipmentDTO> eqDTOs = new ArrayList<>();
        if (rack.getEquipments() != null) {
            eqDTOs = rack.getEquipments().stream()
                    .sorted(java.util.Comparator.comparingInt(e -> e.getSlotPositionU() != null ? e.getSlotPositionU() : 0))
                    .map(this::convertToEquipmentDTO)
                    .collect(Collectors.toList());
        }
        dto.setEquipments(eqDTOs); // Necesitas añadir este campo a RackDTO si no existe

        return dto;
    }

    private EquipmentDTO convertToEquipmentDTO(Equipment eq) {
        EquipmentDTO dto = new EquipmentDTO();
        dto.setId(eq.getId());
        dto.setName(eq.getName());
        dto.setType(eq.getType());
        dto.setSerialNumber(eq.getSerialNumber());
        dto.setPrimaryIp(eq.getPrimaryIp());
        dto.setManagementIp(eq.getManagementIp());
        dto.setVlanId(eq.getVlanId());
        dto.setMacAddress(eq.getMacAddress());
        dto.setSlotPositionU(eq.getSlotPositionU());
        dto.setSlotHeightU(eq.getSlotHeightU());
        dto.setStatus(eq.getStatus());
        // Mapear rack si es necesario
        if (eq.getRack() != null) {
            dto.setRackId(eq.getRack().getId());
            dto.setRackLocationLabel(eq.getRack().getLocationLabel());
        }
        return dto;
    }
}