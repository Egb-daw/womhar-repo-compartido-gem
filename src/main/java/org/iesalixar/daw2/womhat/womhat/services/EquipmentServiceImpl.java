package org.iesalixar.daw2.womhat.womhat.services;

import lombok.RequiredArgsConstructor;
import org.iesalixar.daw2.womhat.womhat.dtos.EquipmentDTO;
import org.iesalixar.daw2.womhat.womhat.dtos.EquipmentDetailDTO;
import org.iesalixar.daw2.womhat.womhat.dtos.EquipmentEventLogDTO;
import org.iesalixar.daw2.womhat.womhat.dtos.EquipmentFormDTO;
import org.iesalixar.daw2.womhat.womhat.entities.*;
import org.iesalixar.daw2.womhat.womhat.enums.EquipmentEventType;
import org.iesalixar.daw2.womhat.womhat.enums.EquipmentLogAction;
import org.iesalixar.daw2.womhat.womhat.enums.EquipmentStatus;
import org.iesalixar.daw2.womhat.womhat.enums.EquipmentType;
import org.iesalixar.daw2.womhat.womhat.enums.RackPermission;
import org.iesalixar.daw2.womhat.womhat.exceptions.DuplicateResourceException;
import org.iesalixar.daw2.womhat.womhat.exceptions.ResourceNotFoundException;
import org.iesalixar.daw2.womhat.womhat.mappers.*;
import org.iesalixar.daw2.womhat.womhat.repositories.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de equipos.
 *
 * Además del CRUD, se encarga de:
 * - validar serial único,
 * - validar ocupación U dentro del rack,
 * - sincronizar especializaciones 1:1,
 * - registrar auditoría básica,
 * - filtrar lectura real por acceso a rack para usuarios limitados.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class EquipmentServiceImpl implements EquipmentService {

    private static final Logger logger = LoggerFactory.getLogger(EquipmentServiceImpl.class);
    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    private final EquipmentRepository equipmentRepository;
    private final RackRepository rackRepository;
    private final HostSpecificationRepository hostSpecificationRepository;
    private final StorageBackupRepository storageBackupRepository;
    private final NetworkElementRepository networkElementRepository;
    private final EquipmentEventLogRepository equipmentEventLogRepository;
    private final UserRepository userRepository;
    private final UserRackAccessRepository userRackAccessRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<EquipmentDTO> list(Pageable pageable) {
        return list(pageable, false);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EquipmentDTO> list(Pageable pageable, boolean includeClosed) {
        List<EquipmentDTO> filtered = equipmentRepository.findAll().stream()
                .filter(equipment -> isVisibleInDefaultList(equipment, includeClosed))
                .sorted(resolveEquipmentComparator(pageable))
                .map(EquipmentMapper::toDTO)
                .toList();

        return toPage(filtered, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EquipmentDTO> list(Pageable pageable, String currentUserEmail) {
        return list(pageable, currentUserEmail, false);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EquipmentDTO> list(Pageable pageable, String currentUserEmail, boolean includeClosed) {
        if (!StringUtils.hasText(currentUserEmail)) {
            return list(pageable, includeClosed);
        }

        User user = findUserByEmail(currentUserEmail);
        if (hasTechnicalAccess(user)) {
            return list(pageable, includeClosed);
        }

        Set<Long> accessibleRackIds = resolveAccessibleRackIds(user);
        if (accessibleRackIds.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        List<EquipmentDTO> filtered = equipmentRepository.findAll().stream()
                .filter(equipment -> equipment.getRack() != null
                        && equipment.getRack().getId() != null
                        && accessibleRackIds.contains(equipment.getRack().getId()))
                .filter(equipment -> isVisibleInDefaultList(equipment, includeClosed))
                .sorted(resolveEquipmentComparator(pageable))
                .map(EquipmentMapper::toDTO)
                .toList();

        return toPage(filtered, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EquipmentDTO> listByRack(Long rackId) {
        return equipmentRepository.findByRack_IdOrderByNameAsc(rackId).stream()
                .sorted(Comparator.comparing(Equipment::getName, Comparator.nullsLast(String::compareToIgnoreCase)))
                .map(EquipmentMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EquipmentDetailDTO getDetail(Long id) {
        Equipment equipment = equipmentRepository.findDetailedById(id)
                .orElseThrow(() -> new ResourceNotFoundException("equipment", "id", id));
        return EquipmentMapper.toDetailDTO(equipment);
    }

    @Override
    @Transactional(readOnly = true)
    public EquipmentDetailDTO getDetail(Long id, String currentUserEmail) {
        Equipment equipment = equipmentRepository.findDetailedById(id)
                .orElseThrow(() -> new ResourceNotFoundException("equipment", "id", id));

        if (!StringUtils.hasText(currentUserEmail)) {
            return EquipmentMapper.toDetailDTO(equipment);
        }

        User user = findUserByEmail(currentUserEmail);
        Long rackId = equipment.getRack() != null ? equipment.getRack().getId() : null;

        if (hasTechnicalAccess(user) || hasAccessToRack(user, rackId)) {
            return EquipmentMapper.toDetailDTO(equipment);
        }

        throw new AccessDeniedException("No tiene permisos para acceder a este equipo.");
    }

    @Override
    @Transactional(readOnly = true)
    public EquipmentFormDTO getForm(Long id) {
        Equipment equipment = equipmentRepository.findDetailedById(id)
                .orElseThrow(() -> new ResourceNotFoundException("equipment", "id", id));
        return EquipmentMapper.toFormDTO(equipment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EquipmentEventLogDTO> getEventLog(Long equipmentId) {
        return EquipmentEventLogMapper.toDTOList(
                equipmentEventLogRepository.findByEquipment_IdOrderByChangedAtDesc(equipmentId)
        );
    }

    @Override
    public void create(EquipmentFormDTO dto, String actorEmail) {
        logger.info("Creando equipo serial={} type={}", dto.getSerialNumber(), dto.getType());

        Rack rack = rackRepository.findDetailedById(dto.getRackId())
                .orElseThrow(() -> new ResourceNotFoundException("rack", "id", dto.getRackId()));

        ensureCanWriteRack(actorEmail, rack.getId());

        equipmentRepository.findBySerialNumberIgnoreCase(dto.getSerialNumber()).ifPresent(existing -> {
            throw new DuplicateResourceException("equipment", "serialNumber", dto.getSerialNumber());
        });

        validateRackSlotAvailability(rack, dto, null);

        Equipment equipment = EquipmentMapper.toEntity(dto, rack);
        equipment = equipmentRepository.save(equipment);

        syncSpecializations(equipment, dto);
        logger.info("Equipo creado id={}, rackId={}, actor={}", equipment.getId(), rack.getId(), actorEmail);

        saveAuditLog(
                equipment,
                actorEmail,
                EquipmentLogAction.INSERT,
                EquipmentEventType.CREATED,
                null,
                rack.getId(),
                null,
                equipment.getStatus(),
                "Equipo creado correctamente."
        );
    }

    @Override
    public void update(EquipmentFormDTO dto, String actorEmail) {
        logger.info("Actualizando equipo id={}", dto.getId());

        Equipment equipment = equipmentRepository.findDetailedById(dto.getId())
                .orElseThrow(() -> new ResourceNotFoundException("equipment", "id", dto.getId()));

        Long oldRackId = equipment.getRack() != null ? equipment.getRack().getId() : null;
        EquipmentStatus oldStatus = equipment.getStatus();

        Rack newRack = rackRepository.findDetailedById(dto.getRackId())
                .orElseThrow(() -> new ResourceNotFoundException("rack", "id", dto.getRackId()));

        ensureCanWriteRack(actorEmail, oldRackId);
        ensureCanWriteRack(actorEmail, newRack.getId());

        equipmentRepository.findBySerialNumberIgnoreCase(dto.getSerialNumber())
                .filter(existing -> !existing.getId().equals(dto.getId()))
                .ifPresent(existing -> {
                    throw new DuplicateResourceException("equipment", "serialNumber", dto.getSerialNumber());
                });

        validateRackSlotAvailability(newRack, dto, equipment.getId());

        EquipmentMapper.copyToExistingEntity(dto, equipment, newRack);
        equipmentRepository.save(equipment);

        syncSpecializations(equipment, dto);
        logger.info("Equipo actualizado id={}, oldRackId={}, newRackId={}, actor={}", equipment.getId(), oldRackId, newRack.getId(), actorEmail);

        EquipmentEventType eventType = resolveEventType(oldRackId, newRack.getId(), oldStatus, equipment.getStatus());

        saveAuditLog(
                equipment,
                actorEmail,
                EquipmentLogAction.UPDATE,
                eventType,
                oldRackId,
                newRack.getId(),
                oldStatus,
                equipment.getStatus(),
                "Equipo actualizado correctamente."
        );
    }

    @Override
    public void changeStatus(Long id, EquipmentStatus status, String actorEmail) {
        logger.info("Cambiando estado de equipo id={} a {}", id, status);

        if (status == null) {
            throw new IllegalArgumentException("El estado del equipo no puede estar vacío.");
        }

        Equipment equipment = equipmentRepository.findDetailedById(id)
                .orElseThrow(() -> new ResourceNotFoundException("equipment", "id", id));

        Long rackId = equipment.getRack() != null ? equipment.getRack().getId() : null;
        ensureCanWriteRack(actorEmail, rackId);

        EquipmentStatus oldStatus = equipment.getStatus();
        if (oldStatus == status) {
            return;
        }

        equipment.setStatus(status);
        equipmentRepository.save(equipment);
        logger.info("Estado de equipo actualizado id={}, oldStatus={}, newStatus={}, actor={}", id, oldStatus, status, actorEmail);

        saveAuditLog(
                equipment,
                actorEmail,
                EquipmentLogAction.UPDATE,
                EquipmentEventType.STATUS_CHANGED,
                rackId,
                rackId,
                oldStatus,
                status,
                "Estado del equipo actualizado de " + (oldStatus != null ? oldStatus.name() : "-") + " a " + status.name() + "."
        );
    }

    @Override
    public void delete(Long id, String actorEmail) {
        logger.info("Eliminando equipo id={}", id);

        ensureGlobalAdmin(actorEmail);

        Equipment equipment = equipmentRepository.findDetailedById(id)
                .orElseThrow(() -> new ResourceNotFoundException("equipment", "id", id));

        Long oldRackId = equipment.getRack() != null ? equipment.getRack().getId() : null;
        EquipmentStatus oldStatus = equipment.getStatus();
        String message = "Equipo eliminado: " + equipment.getName() + " (" + equipment.getSerialNumber() + ")";

        equipmentRepository.delete(equipment);
        logger.info("Equipo eliminado físicamente id={}, rackId={}, actor={}", id, oldRackId, actorEmail);

        EquipmentEventLog log = new EquipmentEventLog();
        log.setEquipment(null);
        log.setChangedBy(resolveActor(actorEmail));
        log.setAction(EquipmentLogAction.DELETE);
        log.setEventType(EquipmentEventType.DELETED);
        log.setOldRackId(oldRackId);
        log.setNewRackId(null);
        log.setOldStatus(oldStatus != null ? oldStatus.name() : null);
        log.setNewStatus(null);
        log.setMessage(message);

        equipmentEventLogRepository.save(log);
    }

    /**
     * Valida que no haya solape en la ocupación U del rack.
     */
    private void validateRackSlotAvailability(Rack rack, EquipmentFormDTO dto, Long equipmentIdToExclude) {
        if (dto.getSlotPositionU() == null || dto.getSlotHeightU() == null) {
            return;
        }

        int start = dto.getSlotPositionU();
        int end = dto.getSlotPositionU() + dto.getSlotHeightU() - 1;

        if (rack.getCapacityU() != null && end > rack.getCapacityU()) {
            throw new IllegalStateException("La ocupación en U supera la capacidad del rack.");
        }

        boolean overlaps = rack.getEquipments().stream()
                .filter(existing -> equipmentIdToExclude == null || !existing.getId().equals(equipmentIdToExclude))
                .filter(existing -> existing.getSlotPositionU() != null && existing.getSlotHeightU() != null)
                .anyMatch(existing -> {
                    int existingStart = existing.getSlotPositionU();
                    int existingEnd = existing.getSlotPositionU() + existing.getSlotHeightU() - 1;
                    return start <= existingEnd && end >= existingStart;
                });

        if (overlaps) {
            throw new IllegalStateException("La posición U del equipo se solapa con otro equipo del mismo rack.");
        }
    }

    /**
     * Sincroniza las tablas 1:1 según el tipo del equipo.
     */
    private void syncSpecializations(Equipment equipment, EquipmentFormDTO dto) {
        if (isHostType(dto.getType())) {
            HostSpecification hostSpecification = hostSpecificationRepository.findByEquipment_Id(equipment.getId())
                    .orElse(null);

            if (hostSpecification == null) {
                hostSpecification = HostSpecificationMapper.toEntity(dto, equipment);
            } else {
                HostSpecificationMapper.copyToExistingEntity(dto, hostSpecification);
            }
            hostSpecificationRepository.save(hostSpecification);
        } else {
            hostSpecificationRepository.findByEquipment_Id(equipment.getId()).ifPresent(hostSpecificationRepository::delete);
        }

        if (isNetworkType(dto.getType())) {
            NetworkElement networkElement = networkElementRepository.findByEquipment_Id(equipment.getId())
                    .orElse(null);

            if (networkElement == null) {
                networkElement = NetworkElementMapper.toEntity(dto, equipment);
            } else {
                NetworkElementMapper.copyToExistingEntity(dto, networkElement);
            }
            networkElementRepository.save(networkElement);
        } else {
            networkElementRepository.findByEquipment_Id(equipment.getId()).ifPresent(networkElementRepository::delete);
        }

        if (isStorageType(dto.getType())) {
            StorageBackup storageBackup = storageBackupRepository.findByEquipment_Id(equipment.getId())
                    .orElse(null);

            if (storageBackup == null) {
                storageBackup = StorageBackupMapper.toEntity(dto, equipment);
            } else {
                StorageBackupMapper.copyToExistingEntity(dto, storageBackup);
            }
            storageBackupRepository.save(storageBackup);
        } else {
            storageBackupRepository.findByEquipment_Id(equipment.getId()).ifPresent(storageBackupRepository::delete);
        }
    }

    /**
     * Resuelve el tipo de evento más representativo para la auditoría.
     */
    private EquipmentEventType resolveEventType(Long oldRackId,
                                                Long newRackId,
                                                EquipmentStatus oldStatus,
                                                EquipmentStatus newStatus) {
        if (oldRackId != null && newRackId != null && !oldRackId.equals(newRackId)) {
            return EquipmentEventType.MOVED_RACK;
        }

        if (oldStatus != null && newStatus != null && oldStatus != newStatus) {
            return EquipmentEventType.STATUS_CHANGED;
        }

        return EquipmentEventType.UPDATED;
    }

    /**
     * Guarda una entrada de log/auditoría.
     */
    private void saveAuditLog(Equipment equipment,
                              String actorEmail,
                              EquipmentLogAction action,
                              EquipmentEventType eventType,
                              Long oldRackId,
                              Long newRackId,
                              EquipmentStatus oldStatus,
                              EquipmentStatus newStatus,
                              String message) {

        EquipmentEventLog log = new EquipmentEventLog();
        log.setEquipment(equipment);
        log.setChangedBy(resolveActor(actorEmail));
        log.setAction(action);
        log.setEventType(eventType);
        log.setOldRackId(oldRackId);
        log.setNewRackId(newRackId);
        log.setOldStatus(oldStatus != null ? oldStatus.name() : null);
        log.setNewStatus(newStatus != null ? newStatus.name() : null);
        log.setMessage(message);

        equipmentEventLogRepository.save(log);
    }

    /**
     * Resuelve el usuario autenticado a partir de su email.
     */
    private User resolveActor(String actorEmail) {
        if (actorEmail == null || actorEmail.isBlank()) {
            return null;
        }
        return userRepository.findByEmailIgnoreCase(actorEmail).orElse(null);
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("user", "email", email));
    }

    private boolean hasTechnicalAccess(User user) {
        return hasRole(user, ROLE_ADMIN);
    }

    private boolean hasRole(User user, String roleName) {
        return user != null
                && user.getRoles() != null
                && user.getRoles().stream()
                .map(Role::getName)
                .anyMatch(roleName::equalsIgnoreCase);
    }

    private boolean hasAccessToRack(User user, Long rackId) {
        return user != null
                && user.getId() != null
                && rackId != null
                && userRackAccessRepository.findByIdUserIdAndIdRackId(user.getId(), rackId).isPresent();
    }

    private void ensureGlobalAdmin(String actorEmail) {
        if (!StringUtils.hasText(actorEmail)) {
            throw new AccessDeniedException("La operación requiere administrador global.");
        }

        User actor = findUserByEmail(actorEmail);
        if (!hasTechnicalAccess(actor)) {
            throw new AccessDeniedException("La operación requiere administrador global.");
        }
    }

    private void ensureCanWriteRack(String actorEmail, Long rackId) {
        if (!StringUtils.hasText(actorEmail) || rackId == null) {
            throw new AccessDeniedException("No tiene permisos para modificar equipos en este rack.");
        }

        User actor = findUserByEmail(actorEmail);
        if (hasTechnicalAccess(actor)) {
            return;
        }

        if (!hasWriteAccessToRack(actor, rackId)) {
            throw new AccessDeniedException("No tiene permisos para modificar equipos en este rack.");
        }
    }

    private boolean hasWriteAccessToRack(User user, Long rackId) {
        return user != null
                && user.getId() != null
                && rackId != null
                && userRackAccessRepository.findByIdUserIdAndIdRackId(user.getId(), rackId)
                .map(UserRackAccess::getPermission)
                .filter(permission -> permissionRank(permission) >= permissionRank(RackPermission.WRITE))
                .isPresent();
    }

    private boolean isVisibleInDefaultList(Equipment equipment, boolean includeClosed) {
        if (includeClosed || equipment == null || equipment.getStatus() == null) {
            return true;
        }

        return equipment.getStatus() != EquipmentStatus.RETIRED
                && equipment.getStatus() != EquipmentStatus.ARCHIVED;
    }

    private Set<Long> resolveAccessibleRackIds(User user) {
        if (user == null || user.getId() == null) {
            return Set.of();
        }

        return userRackAccessRepository.findByIdUserId(user.getId())
                .stream()
                .map(access -> access.getId() != null ? access.getId().getRackId() : null)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private int permissionRank(RackPermission permission) {
        if (permission == null) {
            return 0;
        }

        return switch (permission) {
            case READ -> 1;
            case WRITE -> 2;
            case ADMIN -> 3;
        };
    }

    private Comparator<Equipment> resolveEquipmentComparator(Pageable pageable) {
        Comparator<Equipment> comparator = Comparator.comparing(
                Equipment::getName,
                Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
        );

        if (pageable.getSort().isUnsorted()) {
            return comparator;
        }

        for (Sort.Order order : pageable.getSort()) {
            Comparator<Equipment> nextComparator = switch (order.getProperty()) {
                case "type" -> Comparator.comparing(equipment -> equipment.getType() != null ? equipment.getType().name() : null,
                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
                case "status" -> Comparator.comparing(equipment -> equipment.getStatus() != null ? equipment.getStatus().name() : null,
                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
                case "primaryIp" -> Comparator.comparing(Equipment::getPrimaryIp, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
                case "name" -> Comparator.comparing(Equipment::getName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
                default -> Comparator.comparing(Equipment::getName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
            };

            if (!order.isAscending()) {
                nextComparator = nextComparator.reversed();
            }
            comparator = nextComparator;
        }

        return comparator;
    }

    private <T> Page<T> toPage(List<T> content, Pageable pageable) {
        int start = Math.toIntExact(pageable.getOffset());
        if (start >= content.size()) {
            return new PageImpl<>(List.of(), pageable, content.size());
        }

        int end = Math.min(start + pageable.getPageSize(), content.size());
        return new PageImpl<>(content.subList(start, end), pageable, content.size());
    }

    /**
     * Determina si el tipo requiere host_specifications.
     */
    private boolean isHostType(EquipmentType type) {
        return type == EquipmentType.SERVER;
    }

    /**
     * Determina si el tipo requiere network_elements.
     */
    private boolean isNetworkType(EquipmentType type) {
        return type == EquipmentType.SWITCH
                || type == EquipmentType.ROUTER
                || type == EquipmentType.FIREWALL;
    }

    /**
     * Determina si el tipo requiere storage_backups.
     */
    private boolean isStorageType(EquipmentType type) {
        return type == EquipmentType.NAS || type == EquipmentType.STORAGE;
    }
}
