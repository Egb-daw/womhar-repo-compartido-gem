package org.iesalixar.daw2.womhat.womhat.services;

import lombok.RequiredArgsConstructor;
import org.iesalixar.daw2.womhat.womhat.dtos.RackDTO;
import org.iesalixar.daw2.womhat.womhat.dtos.RackDetailDTO;
import org.iesalixar.daw2.womhat.womhat.dtos.RackFormDTO;
import org.iesalixar.daw2.womhat.womhat.dtos.RackOptionDTO;
import org.iesalixar.daw2.womhat.womhat.entities.DataCenterRoom;
import org.iesalixar.daw2.womhat.womhat.entities.Rack;
import org.iesalixar.daw2.womhat.womhat.entities.Role;
import org.iesalixar.daw2.womhat.womhat.entities.User;
import org.iesalixar.daw2.womhat.womhat.exceptions.DuplicateResourceException;
import org.iesalixar.daw2.womhat.womhat.exceptions.ResourceNotFoundException;
import org.iesalixar.daw2.womhat.womhat.mappers.RackMapper;
import org.iesalixar.daw2.womhat.womhat.repositories.DataCenterRoomRepository;
import org.iesalixar.daw2.womhat.womhat.repositories.RackRepository;
import org.iesalixar.daw2.womhat.womhat.repositories.UserRackAccessRepository;
import org.iesalixar.daw2.womhat.womhat.repositories.UserRepository;
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

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de racks.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class RackServiceImpl implements RackService {

    private static final Logger logger = LoggerFactory.getLogger(RackServiceImpl.class);
    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    private final RackRepository rackRepository;
    private final DataCenterRoomRepository dataCenterRoomRepository;
    private final UserRepository userRepository;
    private final UserRackAccessRepository userRackAccessRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<RackDTO> list(Pageable pageable) {
        return rackRepository.findAll(pageable).map(RackMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RackDTO> list(Pageable pageable, String currentUserEmail) {
        if (!StringUtils.hasText(currentUserEmail)) {
            return list(pageable);
        }

        User user = findUserByEmail(currentUserEmail);
        if (hasTechnicalAccess(user)) {
            return list(pageable);
        }

        Set<Long> accessibleRackIds = resolveAccessibleRackIds(user);
        if (accessibleRackIds.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        List<RackDTO> filtered = rackRepository.findAll().stream()
                .filter(rack -> rack.getId() != null && accessibleRackIds.contains(rack.getId()))
                .sorted(resolveRackComparator(pageable))
                .map(RackMapper::toDTO)
                .toList();

        return toPage(filtered, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RackDTO> listByRoom(Long roomId) {
        return RackMapper.toDTOList(
                rackRepository.findByRoom_IdOrderByLocationLabelAsc(roomId)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<RackOptionDTO> listOptions() {
        return RackMapper.toOptionList(
                rackRepository.findAll(Sort.by(Sort.Direction.ASC, "locationLabel"))
        );
    }

    @Override
    @Transactional(readOnly = true)
    public RackDetailDTO getDetail(Long id) {
        Rack entity = rackRepository.findDetailedById(id)
                .orElseThrow(() -> new ResourceNotFoundException("rack", "id", id));
        return RackMapper.toDetailDTO(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public RackDetailDTO getDetail(Long id, String currentUserEmail) {
        Rack entity = rackRepository.findDetailedById(id)
                .orElseThrow(() -> new ResourceNotFoundException("rack", "id", id));

        if (!StringUtils.hasText(currentUserEmail)) {
            return RackMapper.toDetailDTO(entity);
        }

        User user = findUserByEmail(currentUserEmail);
        if (hasTechnicalAccess(user) || hasAccessToRack(user, entity.getId())) {
            return RackMapper.toDetailDTO(entity);
        }

        throw new AccessDeniedException("No tiene permisos para acceder a este rack.");
    }

    @Override
    @Transactional(readOnly = true)
    public RackFormDTO getForm(Long id) {
        Rack entity = rackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("rack", "id", id));
        return RackMapper.toFormDTO(entity);
    }

    @Override
    public void create(RackFormDTO dto) {
        logger.info("Creando rack locationLabel={}", dto.getLocationLabel());

        DataCenterRoom room = dataCenterRoomRepository.findById(dto.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("dataCenterRoom", "id", dto.getRoomId()));

        rackRepository.findByLocationLabelIgnoreCase(dto.getLocationLabel()).ifPresent(existing -> {
            throw new DuplicateResourceException("rack", "locationLabel", dto.getLocationLabel());
        });

        Rack entity = RackMapper.toEntity(dto, room);
        rackRepository.save(entity);
    }

    @Override
    public void update(RackFormDTO dto) {
        logger.info("Actualizando rack id={}", dto.getId());

        Rack entity = rackRepository.findById(dto.getId())
                .orElseThrow(() -> new ResourceNotFoundException("rack", "id", dto.getId()));

        DataCenterRoom room = dataCenterRoomRepository.findById(dto.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("dataCenterRoom", "id", dto.getRoomId()));

        rackRepository.findByLocationLabelIgnoreCase(dto.getLocationLabel())
                .filter(existing -> !existing.getId().equals(dto.getId()))
                .ifPresent(existing -> {
                    throw new DuplicateResourceException("rack", "locationLabel", dto.getLocationLabel());
                });

        RackMapper.copyToExistingEntity(dto, entity, room);
        rackRepository.save(entity);
    }

    @Override
    public void delete(Long id) {
        logger.info("Eliminando rack id={}", id);

        Rack entity = rackRepository.findDetailedById(id)
                .orElseThrow(() -> new ResourceNotFoundException("rack", "id", id));

        if (entity.getEquipments() != null && !entity.getEquipments().isEmpty()) {
            throw new IllegalStateException("No se puede eliminar el rack porque tiene equipos instalados.");
        }

        rackRepository.delete(entity);
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

    private Comparator<Rack> resolveRackComparator(Pageable pageable) {
        Comparator<Rack> comparator = Comparator.comparing(
                Rack::getLocationLabel,
                Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
        );

        if (pageable.getSort().isUnsorted()) {
            return comparator;
        }

        for (Sort.Order order : pageable.getSort()) {
            Comparator<Rack> nextComparator = switch (order.getProperty()) {
                case "capacityU" -> Comparator.comparing(Rack::getCapacityU, Comparator.nullsLast(Integer::compareTo));
                case "functionName" -> Comparator.comparing(Rack::getFunctionName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
                case "groupName" -> Comparator.comparing(Rack::getGroupName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
                case "status" -> Comparator.comparing(rack -> rack.getStatus() != null ? rack.getStatus().name() : null,
                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
                case "locationLabel" -> Comparator.comparing(Rack::getLocationLabel, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
                default -> Comparator.comparing(Rack::getLocationLabel, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
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
}
