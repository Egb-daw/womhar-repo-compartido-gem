package org.iesalixar.daw2.womhat.womhat.services;

import lombok.RequiredArgsConstructor;
import org.iesalixar.daw2.womhat.womhat.dtos.DashboardRackMapItemDTO;
import org.iesalixar.daw2.womhat.womhat.dtos.DashboardSummaryDTO;
import org.iesalixar.daw2.womhat.womhat.dtos.RecentActivityDTO;
import org.iesalixar.daw2.womhat.womhat.entities.Equipment;
import org.iesalixar.daw2.womhat.womhat.entities.EquipmentEventLog;
import org.iesalixar.daw2.womhat.womhat.entities.Rack;
import org.iesalixar.daw2.womhat.womhat.entities.User;
import org.iesalixar.daw2.womhat.womhat.entities.UserRackAccess;
import org.iesalixar.daw2.womhat.womhat.enums.EquipmentStatus;
import org.iesalixar.daw2.womhat.womhat.enums.EquipmentType;
import org.iesalixar.daw2.womhat.womhat.enums.RackPermission;
import org.iesalixar.daw2.womhat.womhat.enums.WorkOrderStatus;
import org.iesalixar.daw2.womhat.womhat.exceptions.ResourceNotFoundException;
import org.iesalixar.daw2.womhat.womhat.mappers.DashboardMapper;
import org.iesalixar.daw2.womhat.womhat.mappers.EquipmentEventLogMapper;
import org.iesalixar.daw2.womhat.womhat.mappers.RackMapper;
import org.iesalixar.daw2.womhat.womhat.repositories.EquipmentEventLogRepository;
import org.iesalixar.daw2.womhat.womhat.repositories.EquipmentRepository;
import org.iesalixar.daw2.womhat.womhat.repositories.MaintenanceWorkOrderRepository;
import org.iesalixar.daw2.womhat.womhat.repositories.RackRepository;
import org.iesalixar.daw2.womhat.womhat.repositories.UserRackAccessRepository;
import org.iesalixar.daw2.womhat.womhat.repositories.UserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementación del servicio del dashboard.
 *
 * En esta versión:
 * - ADMIN ve el panel completo de backoffice.
 * - USER recibe un panel filtrado por racks visibles, inventario accesible
 *   y actividad reciente relacionada con su alcance real.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    private final RackRepository rackRepository;
    private final EquipmentRepository equipmentRepository;
    private final MaintenanceWorkOrderRepository maintenanceWorkOrderRepository;
    private final EquipmentEventLogRepository equipmentEventLogRepository;
    private final UserRepository userRepository;
    private final UserRackAccessRepository userRackAccessRepository;

    /**
     * Calcula las métricas principales globales.
     */
    private DashboardSummaryDTO getGlobalSummary() {
        return buildSummary(resolveAllRackIds());
    }

    /**
     * Calcula las métricas principales según el usuario autenticado.
     */
    @Override
    public DashboardSummaryDTO getSummaryForUser(String email) {
        if (!StringUtils.hasText(email)) {
            return getGlobalSummary();
        }

        User user = findUserByEmail(email);

        if (hasTechnicalAccess(user)) {
            return getGlobalSummary();
        }

        return buildSummary(resolveAccessibleRackIds(user));
    }

    /**
     * Obtiene los racks para pintarlos en el mapa del dashboard.
     */
    private List<DashboardRackMapItemDTO> getGlobalRackMapItems() {
        return buildRackMap(resolveAllRackIds(), Map.of(), true);
    }

    /**
     * Obtiene los racks visibles para un usuario concreto.
     */
    @Override
    public List<DashboardRackMapItemDTO> getRackMapItemsForUser(String email) {
        if (!StringUtils.hasText(email)) {
            return getGlobalRackMapItems();
        }

        User user = findUserByEmail(email);

        if (hasTechnicalAccess(user)) {
            return getGlobalRackMapItems();
        }

        List<UserRackAccess> accesses = userRackAccessRepository.findDetailedByIdUserId(user.getId());
        Map<Long, UserRackAccess> accessByRackId = buildAccessIndex(accesses);
        return buildRackMap(accessByRackId.keySet(), accessByRackId, false);
    }

    /**
     * Obtiene la actividad reciente global del sistema.
     */
    private List<RecentActivityDTO> getGlobalRecentActivity(int limit) {
        if (limit <= 0) {
            return List.of();
        }

        return equipmentEventLogRepository.findAll(Sort.by(Sort.Direction.DESC, "changedAt"))
                .stream()
                .limit(limit)
                .map(EquipmentEventLogMapper::toRecentActivityDTO)
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * Obtiene la actividad reciente visible para un usuario.
     */
    @Override
    public List<RecentActivityDTO> getRecentActivityForUser(String email, int limit) {
        if (!StringUtils.hasText(email)) {
            return getGlobalRecentActivity(limit);
        }

        if (limit <= 0) {
            return List.of();
        }

        User user = findUserByEmail(email);

        if (hasTechnicalAccess(user)) {
            return getGlobalRecentActivity(limit);
        }

        Set<Long> accessibleRackIds = resolveAccessibleRackIds(user);

        return equipmentEventLogRepository.findAll(Sort.by(Sort.Direction.DESC, "changedAt"))
                .stream()
                .filter(log -> isVisibleForUser(log, user.getId(), accessibleRackIds))
                .limit(limit)
                .map(EquipmentEventLogMapper::toRecentActivityDTO)
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * Construye un resumen real a partir del conjunto de racks visibles.
     */
    private DashboardSummaryDTO buildSummary(Set<Long> visibleRackIds) {
        if (visibleRackIds == null || visibleRackIds.isEmpty()) {
            return new DashboardSummaryDTO(0L, 0L, 0L, 0L);
        }

        List<Equipment> visibleEquipments = equipmentRepository.findAll()
                .stream()
                .filter(equipment ->
                        equipment.getRack() != null
                                && equipment.getRack().getId() != null
                                && visibleRackIds.contains(equipment.getRack().getId()))
                .toList();

        Set<Long> visibleEquipmentIds = visibleEquipments.stream()
                .map(Equipment::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        long totalRacks = visibleRackIds.size();
        long totalEquipments = visibleEquipments.size();

        long activeHosts = visibleEquipments.stream()
                .filter(equipment -> equipment.getType() == EquipmentType.SERVER)
                .filter(equipment -> equipment.getStatus() == EquipmentStatus.ACTIVE)
                .count();

        long alerts = maintenanceWorkOrderRepository.findAll()
                .stream()
                .filter(workOrder ->
                        workOrder.getStatus() == WorkOrderStatus.OPEN
                                || workOrder.getStatus() == WorkOrderStatus.IN_PROGRESS)
                .filter(workOrder ->
                        workOrder.getEquipment() != null
                                && workOrder.getEquipment().getId() != null
                                && visibleEquipmentIds.contains(workOrder.getEquipment().getId()))
                .count();

        return DashboardMapper.toSummaryDTO(totalRacks, totalEquipments, activeHosts, alerts);
    }

    /**
     * Construye el mapa visual del dashboard con racks visibles.
     */
    private List<DashboardRackMapItemDTO> buildRackMap(Set<Long> visibleRackIds,
                                                       Map<Long, UserRackAccess> accessByRackId,
                                                       boolean adminView) {
        if (visibleRackIds == null || visibleRackIds.isEmpty()) {
            return List.of();
        }

        return rackRepository.findAll()
                .stream()
                .filter(rack -> rack.getId() != null && visibleRackIds.contains(rack.getId()))
                .map(rack -> {
                    DashboardRackMapItemDTO dto = RackMapper.toMapItemDTO(rack);
                    if (dto == null) {
                        return null;
                    }

                    if (!adminView && accessByRackId != null && rack.getId() != null) {
                        UserRackAccess access = accessByRackId.get(rack.getId());
                        if (access != null) {
                            dto.setAccessPermission(access.getPermission());
                            dto.setOriginalOwner(access.isOriginalOwner());
                        }
                    }

                    return dto;
                })
                .filter(Objects::nonNull)
                .sorted((left, right) -> {
                    String leftDc = left.getDataCenterName() != null ? left.getDataCenterName() : "";
                    String rightDc = right.getDataCenterName() != null ? right.getDataCenterName() : "";
                    int cmpDc = leftDc.compareToIgnoreCase(rightDc);
                    if (cmpDc != 0) {
                        return cmpDc;
                    }

                    String leftRoom = left.getRoomName() != null ? left.getRoomName() : "";
                    String rightRoom = right.getRoomName() != null ? right.getRoomName() : "";
                    int cmpRoom = leftRoom.compareToIgnoreCase(rightRoom);
                    if (cmpRoom != 0) {
                        return cmpRoom;
                    }

                    String leftLabel = left.getLocationLabel() != null ? left.getLocationLabel() : "";
                    String rightLabel = right.getLocationLabel() != null ? right.getLocationLabel() : "";
                    return leftLabel.compareToIgnoreCase(rightLabel);
                })
                .toList();
    }

    private Map<Long, UserRackAccess> buildAccessIndex(List<UserRackAccess> accesses) {
        if (accesses == null || accesses.isEmpty()) {
            return Map.of();
        }

        return accesses.stream()
                .filter(Objects::nonNull)
                .filter(access -> access.getRack() != null && access.getRack().getId() != null)
                .collect(Collectors.toMap(
                        access -> access.getRack().getId(),
                        access -> access,
                        this::pickHighestAccess
                ));
    }

    private UserRackAccess pickHighestAccess(UserRackAccess left, UserRackAccess right) {
        RackPermission leftPermission = left != null ? left.getPermission() : null;
        RackPermission rightPermission = right != null ? right.getPermission() : null;

        int leftRank = rankPermission(leftPermission);
        int rightRank = rankPermission(rightPermission);

        if (rightRank > leftRank) {
            return right;
        }

        if (leftRank > rightRank) {
            return left;
        }

        boolean leftOwner = left != null && left.isOriginalOwner();
        boolean rightOwner = right != null && right.isOriginalOwner();
        if (!leftOwner && rightOwner) {
            return right;
        }

        return left;
    }

    private int rankPermission(RackPermission permission) {
        if (permission == null) {
            return 0;
        }
        return switch (permission) {
            case READ -> 1;
            case WRITE -> 2;
            case ADMIN -> 3;
        };
    }

    /**
     * Resuelve los racks visibles para el usuario según su acceso real.
     */
    private Set<Long> resolveAccessibleRackIds(User user) {
        if (user == null || user.getId() == null) {
            return Set.of();
        }

        if (hasTechnicalAccess(user)) {
            return resolveAllRackIds();
        }

        return userRackAccessRepository.findByIdUserId(user.getId())
                .stream()
                .map(access -> access.getId() != null ? access.getId().getRackId() : null)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * Resuelve todos los ids de rack del sistema.
     */
    private Set<Long> resolveAllRackIds() {
        return rackRepository.findAll()
                .stream()
                .map(Rack::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * Decide si un usuario tiene alcance técnico completo sobre el panel.
     */
    private boolean hasTechnicalAccess(User user) {
        return hasRole(user, ROLE_ADMIN);
    }

    /**
     * Comprueba si un usuario tiene un rol concreto.
     */
    private boolean hasRole(User user, String roleName) {
        return user != null
                && user.getRoles() != null
                && user.getRoles().stream()
                .anyMatch(role -> role != null && roleName.equalsIgnoreCase(role.getName()));
    }

    /**
     * Comprueba si una actividad es visible para un usuario limitado.
     */
    private boolean isVisibleForUser(EquipmentEventLog log, Long userId, Set<Long> accessibleRackIds) {
        if (log == null) {
            return false;
        }

        boolean sameActor = log.getChangedBy() != null
                && log.getChangedBy().getId() != null
                && log.getChangedBy().getId().equals(userId);

        boolean sameRackScope = log.getEquipment() != null
                && log.getEquipment().getRack() != null
                && log.getEquipment().getRack().getId() != null
                && accessibleRackIds.contains(log.getEquipment().getRack().getId());

        boolean sameOldRackScope = log.getOldRackId() != null
                && accessibleRackIds.contains(log.getOldRackId());

        boolean sameNewRackScope = log.getNewRackId() != null
                && accessibleRackIds.contains(log.getNewRackId());

        return sameActor || sameRackScope || sameOldRackScope || sameNewRackScope;
    }

    /**
     * Recupera un usuario por email.
     */
    private User findUserByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("user", "email", email));
    }
}
