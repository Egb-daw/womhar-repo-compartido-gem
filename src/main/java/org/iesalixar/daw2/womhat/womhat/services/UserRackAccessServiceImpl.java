package org.iesalixar.daw2.womhat.womhat.services;

import lombok.RequiredArgsConstructor;
import org.iesalixar.daw2.womhat.womhat.dtos.RackOptionDTO;
import org.iesalixar.daw2.womhat.womhat.dtos.UserRackAccessDTO;
import org.iesalixar.daw2.womhat.womhat.entities.Rack;
import org.iesalixar.daw2.womhat.womhat.entities.Role;
import org.iesalixar.daw2.womhat.womhat.entities.User;
import org.iesalixar.daw2.womhat.womhat.entities.UserRackAccess;
import org.iesalixar.daw2.womhat.womhat.entities.UserRackAccessId;
import org.iesalixar.daw2.womhat.womhat.enums.RackPermission;
import org.iesalixar.daw2.womhat.womhat.exceptions.ResourceNotFoundException;
import org.iesalixar.daw2.womhat.womhat.mappers.RackMapper;
import org.iesalixar.daw2.womhat.womhat.mappers.UserRackAccessMapper;
import org.iesalixar.daw2.womhat.womhat.repositories.RackRepository;
import org.iesalixar.daw2.womhat.womhat.repositories.UserRackAccessRepository;
import org.iesalixar.daw2.womhat.womhat.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Implementación del servicio de permisos usuario-rack.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class UserRackAccessServiceImpl implements UserRackAccessService {

    private static final Logger logger = LoggerFactory.getLogger(UserRackAccessServiceImpl.class);
    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    private final UserRackAccessRepository userRackAccessRepository;
    private final UserRepository userRepository;
    private final RackRepository rackRepository;

    @Override
    @Transactional(readOnly = true)
    public List<UserRackAccessDTO> listByUser(Long userId) {
        return UserRackAccessMapper.toDTOList(
                userRackAccessRepository.findDetailedByIdUserId(userId)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserRackAccessDTO> listByRack(Long rackId) {
        return UserRackAccessMapper.toDTOList(
                userRackAccessRepository.findDetailedByIdRackId(rackId)
        );
    }

    @Override
    public void grantOrUpdate(Long userId, Long rackId, RackPermission permission) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("user", "id", userId));

        saveAccess(user, rackId, permission, null, false);
        logger.info("Acceso concedido/actualizado por id: userId={}, rackId={}, permission={}",
                userId, rackId, permission);
    }

    @Override
    public void grantOrUpdateByEmail(String userEmail, Long rackId, RackPermission permission) {
        if (!StringUtils.hasText(userEmail)) {
            throw new ResourceNotFoundException("user", "email", userEmail);
        }

        User user = findUserByEmail(userEmail.trim());

        saveAccess(user, rackId, permission, null, false);
        logger.info("Acceso concedido/actualizado por email: targetEmail={}, rackId={}, permission={}",
                user.getEmail(), rackId, permission);
    }

    @Override
    public void grantOrUpdateByEmail(String actorEmail, String userEmail, Long rackId, RackPermission permission) {
        User actor = findUserByEmail(actorEmail);
        if (!canManageRackAccess(actorEmail, rackId)) {
            logger.warn("Intento denegado de delegación de acceso: actor={}, rackId={}, targetEmail={}",
                    actorEmail, rackId, userEmail);
            throw new AccessDeniedException("No tiene permisos para compartir este rack.");
        }

        User target = findUserByEmail(userEmail);
        UserRackAccess existing = userRackAccessRepository.findByIdUserIdAndIdRackId(target.getId(), rackId).orElse(null);
        if (existing != null && existing.isOriginalOwner() && !hasRole(actor, ROLE_ADMIN)) {
            logger.warn("Intento denegado de modificación de propietario original: actor={}, rackId={}, targetUserId={}",
                    actorEmail, rackId, target.getId());
            throw new AccessDeniedException("No puede modificar al propietario original del rack.");
        }

        saveAccess(target, rackId, permission, actor, false);
        logger.info("Acceso delegado actualizado: actor={}, target={}, rackId={}, permission={}",
                actorEmail, target.getEmail(), rackId, permission);
    }

    @Override
    public void grantOriginalOwnerByEmail(String userEmail, Long rackId) {
        grantOriginalOwnerByEmail(null, userEmail, rackId);
    }

    @Override
    public void grantOriginalOwnerByEmail(String actorEmail, String userEmail, Long rackId) {
        User user = findUserByEmail(userEmail);
        User actor = StringUtils.hasText(actorEmail) ? findUserByEmail(actorEmail) : null;
        if (actor != null && !hasRole(actor, ROLE_ADMIN)) {
            logger.warn("Intento denegado de asignación de propietario original: actor={}, rackId={}, targetEmail={}",
                    actorEmail, rackId, userEmail);
            throw new AccessDeniedException("Solo un administrador global puede asignar propietario funcional original desde un pedido.");
        }

        boolean rackAlreadyHasOriginalOwner = userRackAccessRepository
                .findFirstByIdRackIdAndOriginalOwnerTrue(rackId)
                .isPresent();

        saveAccess(user, rackId, RackPermission.ADMIN, actor, !rackAlreadyHasOriginalOwner);
        logger.info("Propietario funcional asignado: actor={}, target={}, rackId={}, originalOwnerAssigned={}",
                actorEmail, user.getEmail(), rackId, !rackAlreadyHasOriginalOwner);
    }

    /**
     * Crea o actualiza un acceso usuario-rack.
     */
    private void saveAccess(User user, Long rackId, RackPermission permission, User grantedBy, boolean originalOwner) {
        Rack rack = rackRepository.findById(rackId)
                .orElseThrow(() -> new ResourceNotFoundException("rack", "id", rackId));

        UserRackAccess access = userRackAccessRepository.findByIdUserIdAndIdRackId(user.getId(), rackId)
                .orElse(null);

        if (access == null) {
            access = UserRackAccessMapper.toEntity(user, rack, permission);
        } else {
            access.setPermission(permission);
        }

        access.setOriginalOwner(access.isOriginalOwner() || originalOwner);
        access.setGrantedBy(grantedBy);
        access.setGrantedAt(LocalDateTime.now());
        userRackAccessRepository.save(access);
    }

    @Override
    public void revoke(Long userId, Long rackId) {
        UserRackAccessId id = new UserRackAccessId(userId, rackId);

        if (!userRackAccessRepository.existsById(id)) {
            throw new ResourceNotFoundException("userRackAccess", "id", id.toString());
        }

        userRackAccessRepository.deleteById(id);
        logger.info("Acceso revocado por id: userId={}, rackId={}", userId, rackId);
    }

    @Override
    public void revoke(String actorEmail, Long userId, Long rackId) {
        User actor = findUserByEmail(actorEmail);
        if (!canManageRackAccess(actorEmail, rackId)) {
            logger.warn("Intento denegado de revocación de acceso: actor={}, rackId={}, targetUserId={}",
                    actorEmail, rackId, userId);
            throw new AccessDeniedException("No tiene permisos para revocar accesos de este rack.");
        }

        UserRackAccess access = userRackAccessRepository.findByIdUserIdAndIdRackId(userId, rackId)
                .orElseThrow(() -> new ResourceNotFoundException("userRackAccess", "id", new UserRackAccessId(userId, rackId).toString()));

        if (access.isOriginalOwner() && !hasRole(actor, ROLE_ADMIN)) {
            logger.warn("Intento denegado de revocación sobre propietario original: actor={}, rackId={}, targetUserId={}",
                    actorEmail, rackId, userId);
            throw new AccessDeniedException("No puede revocar al propietario original del rack.");
        }

        userRackAccessRepository.delete(access);
        logger.info("Acceso revocado por actor: actor={}, targetUserId={}, rackId={}",
                actorEmail, userId, rackId);
    }

    @Override
    @Transactional(readOnly = true)
    public RackPermission resolvePermission(String userEmail, Long rackId) {
        if (!StringUtils.hasText(userEmail) || rackId == null) {
            return null;
        }

        User user = findUserByEmail(userEmail);
        if (hasRole(user, ROLE_ADMIN)) {
            return RackPermission.ADMIN;
        }

        return userRackAccessRepository.findByIdUserIdAndIdRackId(user.getId(), rackId)
                .map(UserRackAccess::getPermission)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasGlobalAdminAccess(String userEmail) {
        if (!StringUtils.hasText(userEmail)) {
            return false;
        }

        return hasRole(findUserByEmail(userEmail), ROLE_ADMIN);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canReadRack(String userEmail, Long rackId) {
        return hasAtLeastPermission(userEmail, rackId, RackPermission.READ);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canWriteRack(String userEmail, Long rackId) {
        return hasAtLeastPermission(userEmail, rackId, RackPermission.WRITE);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canManageRackAccess(String userEmail, Long rackId) {
        return hasAtLeastPermission(userEmail, rackId, RackPermission.ADMIN);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isOriginalOwner(String userEmail, Long rackId) {
        if (!StringUtils.hasText(userEmail) || rackId == null) {
            return false;
        }

        User user = findUserByEmail(userEmail);
        return userRackAccessRepository.findByIdUserIdAndIdRackId(user.getId(), rackId)
                .map(UserRackAccess::isOriginalOwner)
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public UserRackAccessDTO findOriginalOwner(Long rackId) {
        return userRackAccessRepository.findFirstByIdRackIdAndOriginalOwnerTrue(rackId)
                .map(UserRackAccessMapper::toDTO)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RackOptionDTO> listWritableRackOptions(String userEmail) {
        if (!StringUtils.hasText(userEmail)) {
            return List.of();
        }

        User user = findUserByEmail(userEmail);
        if (hasRole(user, ROLE_ADMIN)) {
            return RackMapper.toOptionList(rackRepository.findAll());
        }

        List<Rack> writableRacks = userRackAccessRepository.findByIdUserId(user.getId()).stream()
                .filter(access -> access.getPermission() != null && permissionRank(access.getPermission()) >= permissionRank(RackPermission.WRITE))
                .map(UserRackAccess::getRack)
                .filter(Objects::nonNull)
                .toList();

        return RackMapper.toOptionList(writableRacks);
    }

    private boolean hasAtLeastPermission(String userEmail, Long rackId, RackPermission requiredPermission) {
        if (!StringUtils.hasText(userEmail) || rackId == null) {
            return false;
        }

        User user = findUserByEmail(userEmail);
        if (hasRole(user, ROLE_ADMIN)) {
            return true;
        }

        return userRackAccessRepository.findByIdUserIdAndIdRackId(user.getId(), rackId)
                .map(UserRackAccess::getPermission)
                .filter(permission -> permissionRank(permission) >= permissionRank(requiredPermission))
                .isPresent();
    }

    /**
     * Resuelve el usuario por email.
     */
    private User findUserByEmail(String email) {
        if (!StringUtils.hasText(email)) {
            throw new ResourceNotFoundException("user", "email", email);
        }

        return userRepository.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new ResourceNotFoundException("user", "email", email.trim()));
    }

    private boolean hasRole(User user, String roleName) {
        return user != null
                && user.getRoles() != null
                && user.getRoles().stream()
                .map(Role::getName)
                .anyMatch(roleName::equalsIgnoreCase);
    }

    /**
     * Convierte permisos a un ranking ordinal para comparar niveles de acceso.
     */
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
}
