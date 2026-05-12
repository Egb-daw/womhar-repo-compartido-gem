package org.iesalixar.daw2.womhat.womhat.services;

import lombok.RequiredArgsConstructor;
import org.iesalixar.daw2.womhat.womhat.dtos.MaintenanceWorkOrderDTO;
import org.iesalixar.daw2.womhat.womhat.dtos.MaintenanceWorkOrderDetailDTO;
import org.iesalixar.daw2.womhat.womhat.dtos.MaintenanceWorkOrderFormDTO;
import org.iesalixar.daw2.womhat.womhat.entities.Equipment;
import org.iesalixar.daw2.womhat.womhat.entities.EquipmentEventLog;
import org.iesalixar.daw2.womhat.womhat.entities.MaintenanceWorkOrder;
import org.iesalixar.daw2.womhat.womhat.entities.User;
import org.iesalixar.daw2.womhat.womhat.enums.EquipmentEventType;
import org.iesalixar.daw2.womhat.womhat.enums.EquipmentLogAction;
import org.iesalixar.daw2.womhat.womhat.enums.WorkOrderPriority;
import org.iesalixar.daw2.womhat.womhat.enums.WorkOrderStatus;
import org.iesalixar.daw2.womhat.womhat.exceptions.ResourceNotFoundException;
import org.iesalixar.daw2.womhat.womhat.mappers.MaintenanceWorkOrderMapper;
import org.iesalixar.daw2.womhat.womhat.repositories.EquipmentRepository;
import org.iesalixar.daw2.womhat.womhat.repositories.EquipmentEventLogRepository;
import org.iesalixar.daw2.womhat.womhat.repositories.MaintenanceWorkOrderRepository;
import org.iesalixar.daw2.womhat.womhat.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementación del servicio de órdenes de mantenimiento.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class MaintenanceWorkOrderServiceImpl implements MaintenanceWorkOrderService {

    private static final Logger logger = LoggerFactory.getLogger(MaintenanceWorkOrderServiceImpl.class);
    private static final String ACCESS_DENIED_MESSAGE = "No tiene permisos para acceder al mantenimiento de este recurso.";

    private final MaintenanceWorkOrderRepository maintenanceWorkOrderRepository;
    private final EquipmentRepository equipmentRepository;
    private final UserRepository userRepository;
    private final UserRackAccessService userRackAccessService;
    private final EquipmentEventLogRepository equipmentEventLogRepository;

    @Override
    @Transactional(readOnly = true)
    public List<MaintenanceWorkOrderDTO> listByEquipment(Long equipmentId, String actorEmail) {
        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("equipment", "id", equipmentId));
        ensureCanReadRack(actorEmail, resolveRackId(equipment));

        return MaintenanceWorkOrderMapper.toDTOList(
                maintenanceWorkOrderRepository.findByEquipment_IdOrderByOpenedAtDesc(equipmentId)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<MaintenanceWorkOrderDTO> listByStatus(WorkOrderStatus status, String actorEmail) {
        return MaintenanceWorkOrderMapper.toDTOList(filterByReadableAccess(
                maintenanceWorkOrderRepository.findByStatusOrderByOpenedAtDesc(status),
                actorEmail
        ));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MaintenanceWorkOrderDTO> listByPriority(WorkOrderPriority priority, String actorEmail) {
        return MaintenanceWorkOrderMapper.toDTOList(filterByReadableAccess(
                maintenanceWorkOrderRepository.findByPriorityOrderByOpenedAtDesc(priority),
                actorEmail
        ));
    }

    @Override
    @Transactional(readOnly = true)
    public MaintenanceWorkOrderDetailDTO getDetail(Long id, String actorEmail) {
        MaintenanceWorkOrder entity = maintenanceWorkOrderRepository.findDetailedById(id)
                .orElseThrow(() -> new ResourceNotFoundException("maintenanceWorkOrder", "id", id));
        ensureCanReadRack(actorEmail, resolveRackId(entity.getEquipment()));
        return MaintenanceWorkOrderMapper.toDetailDTO(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public MaintenanceWorkOrderFormDTO getForm(Long id, String actorEmail) {
        MaintenanceWorkOrder entity = maintenanceWorkOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("maintenanceWorkOrder", "id", id));
        ensureCanWriteRack(actorEmail, resolveRackId(entity.getEquipment()));
        return MaintenanceWorkOrderMapper.toFormDTO(entity);
    }

    @Override
    public void create(MaintenanceWorkOrderFormDTO dto, String actorEmail) {
        logger.info("Creando orden de mantenimiento para equipmentId={}", dto.getEquipmentId());

        Equipment equipment = equipmentRepository.findById(dto.getEquipmentId())
                .orElseThrow(() -> new ResourceNotFoundException("equipment", "id", dto.getEquipmentId()));
        Long rackId = resolveRackId(equipment);
        ensureCanWriteRack(actorEmail, rackId);

        User actor = resolveActor(actorEmail);

        MaintenanceWorkOrder entity = MaintenanceWorkOrderMapper.toEntity(dto, equipment, actor);
        normalizeClosedAt(entity);
        maintenanceWorkOrderRepository.save(entity);
        logger.info("Orden de mantenimiento creada id={}, equipmentId={}, actor={}", entity.getId(), equipment.getId(), actorEmail);

        saveEquipmentAuditLog(
                equipment,
                actor,
                EquipmentLogAction.UPDATE,
                entity.getStatus() == WorkOrderStatus.CLOSED ? EquipmentEventType.STATUS_CHANGED : EquipmentEventType.UPDATED,
                "Orden de mantenimiento creada (#" + entity.getId() + "): " + safeSummary(entity.getSummary())
        );
    }

    @Override
    public void update(MaintenanceWorkOrderFormDTO dto, String actorEmail) {
        logger.info("Actualizando orden de mantenimiento id={}", dto.getId());

        MaintenanceWorkOrder entity = maintenanceWorkOrderRepository.findById(dto.getId())
                .orElseThrow(() -> new ResourceNotFoundException("maintenanceWorkOrder", "id", dto.getId()));
        Long previousRackId = resolveRackId(entity.getEquipment());
        ensureCanWriteRack(actorEmail, previousRackId);

        Equipment equipment = equipmentRepository.findById(dto.getEquipmentId())
                .orElseThrow(() -> new ResourceNotFoundException("equipment", "id", dto.getEquipmentId()));
        Long targetRackId = resolveRackId(equipment);
        ensureCanWriteRack(actorEmail, targetRackId);

        WorkOrderStatus previousStatus = entity.getStatus();
        User actor = resolveActor(actorEmail);

        MaintenanceWorkOrderMapper.copyToExistingEntity(dto, entity, equipment);
        normalizeClosedAt(entity);

        maintenanceWorkOrderRepository.save(entity);
        logger.info("Orden de mantenimiento actualizada id={}, previousStatus={}, newStatus={}, actor={}",
                entity.getId(), previousStatus, entity.getStatus(), actorEmail);

        EquipmentEventType eventType = previousStatus != entity.getStatus()
                ? EquipmentEventType.STATUS_CHANGED
                : EquipmentEventType.UPDATED;
        String message = previousStatus != entity.getStatus()
                ? "Orden de mantenimiento #" + entity.getId() + " cambió de estado a " + entity.getStatus() + "."
                : "Orden de mantenimiento actualizada (#" + entity.getId() + ").";

        saveEquipmentAuditLog(equipment, actor, EquipmentLogAction.UPDATE, eventType, message);
    }

    @Override
    public void delete(Long id, String actorEmail) {
        logger.info("Eliminando orden de mantenimiento id={}", id);
        ensureGlobalAdmin(actorEmail);

        MaintenanceWorkOrder entity = maintenanceWorkOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("maintenanceWorkOrder", "id", id));
        Equipment equipment = entity.getEquipment();
        User actor = resolveActor(actorEmail);

        maintenanceWorkOrderRepository.delete(entity);
        logger.info("Orden de mantenimiento eliminada id={}, actor={}", id, actorEmail);

        if (equipment != null) {
            saveEquipmentAuditLog(
                    equipment,
                    actor,
                    EquipmentLogAction.UPDATE,
                    EquipmentEventType.UPDATED,
                    "Orden de mantenimiento eliminada (#" + id + ")."
            );
        }
    }

    /**
     * Ajusta la fecha de cierre según el estado actual.
     */
    private void normalizeClosedAt(MaintenanceWorkOrder entity) {
        if (entity.getStatus() == WorkOrderStatus.CLOSED && entity.getClosedAt() == null) {
            entity.setClosedAt(LocalDateTime.now());
        }

        if (entity.getStatus() != WorkOrderStatus.CLOSED) {
            entity.setClosedAt(null);
        }
    }

    /**
     * Resuelve el usuario autenticado que crea la orden.
     */
    private User resolveActor(String actorEmail) {
        if (actorEmail == null || actorEmail.isBlank()) {
            return null;
        }
        return userRepository.findByEmailIgnoreCase(actorEmail).orElse(null);
    }

    private List<MaintenanceWorkOrder> filterByReadableAccess(List<MaintenanceWorkOrder> source, String actorEmail) {
        if (userRackAccessService.hasGlobalAdminAccess(actorEmail)) {
            return source;
        }

        return source.stream()
                .filter(order -> {
                    Long rackId = resolveRackId(order.getEquipment());
                    return rackId != null && userRackAccessService.canReadRack(actorEmail, rackId);
                })
                .toList();
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

    private Long resolveRackId(Equipment equipment) {
        if (equipment == null || equipment.getRack() == null) {
            return null;
        }
        return equipment.getRack().getId();
    }

    private void saveEquipmentAuditLog(Equipment equipment,
                                       User actor,
                                       EquipmentLogAction action,
                                       EquipmentEventType eventType,
                                       String message) {
        if (equipment == null) {
            return;
        }

        EquipmentEventLog log = new EquipmentEventLog();
        log.setEquipment(equipment);
        log.setChangedBy(actor);
        log.setAction(action);
        log.setEventType(eventType);
        log.setOldRackId(resolveRackId(equipment));
        log.setNewRackId(resolveRackId(equipment));
        log.setOldStatus(equipment.getStatus() != null ? equipment.getStatus().name() : null);
        log.setNewStatus(equipment.getStatus() != null ? equipment.getStatus().name() : null);
        log.setMessage(message);
        equipmentEventLogRepository.save(log);
    }

    private String safeSummary(String summary) {
        if (!StringUtils.hasText(summary)) {
            return "-";
        }
        return summary.length() > 120 ? summary.substring(0, 117) + "..." : summary;
    }
}
