package org.iesalixar.daw2.womhat.womhat.services;

import lombok.RequiredArgsConstructor;
import org.iesalixar.daw2.womhat.womhat.dtos.MaintenanceNoteDTO;
import org.iesalixar.daw2.womhat.womhat.dtos.MaintenanceNoteFormDTO;
import org.iesalixar.daw2.womhat.womhat.entities.Equipment;
import org.iesalixar.daw2.womhat.womhat.entities.EquipmentEventLog;
import org.iesalixar.daw2.womhat.womhat.entities.MaintenanceNote;
import org.iesalixar.daw2.womhat.womhat.entities.MaintenanceWorkOrder;
import org.iesalixar.daw2.womhat.womhat.entities.User;
import org.iesalixar.daw2.womhat.womhat.enums.EquipmentEventType;
import org.iesalixar.daw2.womhat.womhat.enums.EquipmentLogAction;
import org.iesalixar.daw2.womhat.womhat.exceptions.ResourceNotFoundException;
import org.iesalixar.daw2.womhat.womhat.mappers.MaintenanceNoteMapper;
import org.iesalixar.daw2.womhat.womhat.repositories.EquipmentEventLogRepository;
import org.iesalixar.daw2.womhat.womhat.repositories.MaintenanceNoteRepository;
import org.iesalixar.daw2.womhat.womhat.repositories.MaintenanceWorkOrderRepository;
import org.iesalixar.daw2.womhat.womhat.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Implementación del servicio de notas de mantenimiento.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class MaintenanceNoteServiceImpl implements MaintenanceNoteService {

    private static final Logger logger = LoggerFactory.getLogger(MaintenanceNoteServiceImpl.class);
    private static final String ACCESS_DENIED_MESSAGE = "No tiene permisos para acceder al mantenimiento de este recurso.";

    private final MaintenanceNoteRepository maintenanceNoteRepository;
    private final MaintenanceWorkOrderRepository maintenanceWorkOrderRepository;
    private final UserRepository userRepository;
    private final UserRackAccessService userRackAccessService;
    private final EquipmentEventLogRepository equipmentEventLogRepository;

    @Override
    @Transactional(readOnly = true)
    public List<MaintenanceNoteDTO> listByWorkOrder(Long workOrderId, String actorEmail) {
        MaintenanceWorkOrder workOrder = maintenanceWorkOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("maintenanceWorkOrder", "id", workOrderId));
        ensureCanReadRack(actorEmail, resolveRackId(workOrder));

        return MaintenanceNoteMapper.toDTOList(
                maintenanceNoteRepository.findDetailedByWorkOrder_IdOrderByCreatedAtAsc(workOrderId)
        );
    }

    @Override
    public void create(MaintenanceNoteFormDTO dto, String actorEmail) {
        logger.info("Creando nota de mantenimiento para workOrderId={}", dto.getWorkOrderId());

        MaintenanceWorkOrder workOrder = maintenanceWorkOrderRepository.findById(dto.getWorkOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("maintenanceWorkOrder", "id", dto.getWorkOrderId()));
        ensureCanWriteRack(actorEmail, resolveRackId(workOrder));

        User actor = null;
        if (actorEmail != null && !actorEmail.isBlank()) {
            actor = userRepository.findByEmailIgnoreCase(actorEmail).orElse(null);
        }

        MaintenanceNote entity = MaintenanceNoteMapper.toEntity(dto, workOrder, actor);
        maintenanceNoteRepository.save(entity);
        logger.info("Nota de mantenimiento creada id={}, workOrderId={}, actor={}", entity.getId(), workOrder.getId(), actorEmail);

        saveEquipmentAuditLog(
                workOrder.getEquipment(),
                actor,
                "Nota de mantenimiento añadida a la orden #" + workOrder.getId() + "."
        );
    }

    @Override
    public void delete(Long id, String actorEmail) {
        logger.info("Eliminando nota de mantenimiento id={}", id);
        ensureGlobalAdmin(actorEmail);

        MaintenanceNote entity = maintenanceNoteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("maintenanceNote", "id", id));
        User actor = resolveActor(actorEmail);
        MaintenanceWorkOrder workOrder = entity.getWorkOrder();

        maintenanceNoteRepository.delete(entity);
        logger.info("Nota de mantenimiento eliminada id={}, workOrderId={}, actor={}", id, workOrder != null ? workOrder.getId() : null, actorEmail);

        if (workOrder != null && workOrder.getEquipment() != null) {
            saveEquipmentAuditLog(
                    workOrder.getEquipment(),
                    actor,
                    "Nota de mantenimiento eliminada de la orden #" + workOrder.getId() + "."
            );
        }
    }

    private void ensureCanReadRack(String actorEmail, Long rackId) {
        if (rackId == null || !StringUtils.hasText(actorEmail)) {
            throw new AccessDeniedException(ACCESS_DENIED_MESSAGE);
        }

        if (userRackAccessService.hasGlobalAdminAccess(actorEmail)) {
            return;
        }

        if (!userRackAccessService.canReadRack(actorEmail, rackId)) {
            throw new AccessDeniedException(ACCESS_DENIED_MESSAGE);
        }
    }

    private void ensureCanWriteRack(String actorEmail, Long rackId) {
        if (rackId == null || !StringUtils.hasText(actorEmail)) {
            throw new AccessDeniedException(ACCESS_DENIED_MESSAGE);
        }

        if (userRackAccessService.hasGlobalAdminAccess(actorEmail)) {
            return;
        }

        if (!userRackAccessService.canWriteRack(actorEmail, rackId)) {
            throw new AccessDeniedException(ACCESS_DENIED_MESSAGE);
        }
    }

    private void ensureGlobalAdmin(String actorEmail) {
        if (!StringUtils.hasText(actorEmail) || !userRackAccessService.hasGlobalAdminAccess(actorEmail)) {
            throw new AccessDeniedException("Esta acción está reservada a administración global.");
        }
    }

    private Long resolveRackId(MaintenanceWorkOrder workOrder) {
        if (workOrder == null || workOrder.getEquipment() == null || workOrder.getEquipment().getRack() == null) {
            return null;
        }
        return workOrder.getEquipment().getRack().getId();
    }

    private User resolveActor(String actorEmail) {
        if (!StringUtils.hasText(actorEmail)) {
            return null;
        }
        return userRepository.findByEmailIgnoreCase(actorEmail).orElse(null);
    }

    private void saveEquipmentAuditLog(Equipment equipment, User actor, String message) {
        if (equipment == null) {
            return;
        }

        EquipmentEventLog log = new EquipmentEventLog();
        log.setEquipment(equipment);
        log.setChangedBy(actor);
        log.setAction(EquipmentLogAction.UPDATE);
        log.setEventType(EquipmentEventType.UPDATED);
        Long rackId = equipment.getRack() != null ? equipment.getRack().getId() : null;
        log.setOldRackId(rackId);
        log.setNewRackId(rackId);
        log.setOldStatus(equipment.getStatus() != null ? equipment.getStatus().name() : null);
        log.setNewStatus(equipment.getStatus() != null ? equipment.getStatus().name() : null);
        log.setMessage(message);
        equipmentEventLogRepository.save(log);
    }
}
