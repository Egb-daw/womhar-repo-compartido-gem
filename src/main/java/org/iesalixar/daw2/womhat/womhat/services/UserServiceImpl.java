package org.iesalixar.daw2.womhat.womhat.services;

import lombok.RequiredArgsConstructor;
import org.iesalixar.daw2.womhat.womhat.dtos.UserCreateDTO;
import org.iesalixar.daw2.womhat.womhat.dtos.UserDTO;
import org.iesalixar.daw2.womhat.womhat.dtos.UserDetailDTO;
import org.iesalixar.daw2.womhat.womhat.dtos.UserUpdateDTO;
import org.iesalixar.daw2.womhat.womhat.entities.Role;
import org.iesalixar.daw2.womhat.womhat.entities.User;
import org.iesalixar.daw2.womhat.womhat.exceptions.DuplicateResourceException;
import org.iesalixar.daw2.womhat.womhat.exceptions.ResourceNotFoundException;
import org.iesalixar.daw2.womhat.womhat.mappers.UserMapper;
import org.iesalixar.daw2.womhat.womhat.repositories.RoleRepository;
import org.iesalixar.daw2.womhat.womhat.repositories.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Implementación del CRUD de usuarios.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final int PASSWORD_EXPIRY_DAYS = 90;
    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    /**
     * Lista paginada de usuarios.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<UserDTO> list(Pageable pageable) {
        return userRepository.findAll(pageable).map(UserMapper::toDTO);
    }

    /**
     * Recupera el usuario para edición.
     */
    @Override
    @Transactional(readOnly = true)
    public UserUpdateDTO getForEdit(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("user", "id", id));
        return UserMapper.toUpdateDTO(user);
    }

    /**
     * Crea un usuario nuevo.
     */
    @Override
    public void create(UserCreateDTO dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("user", "email", dto.getEmail());
        }

        normalizePasswordDates(dto);
        Set<Role> roles = resolveRoles(dto.getRoleIds());

        User user = UserMapper.toEntity(dto, roles);
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));

        userRepository.save(user);
    }

    /**
     * Actualiza un usuario existente.
     */
    @Override
    public void update(UserUpdateDTO dto) {
        if (userRepository.existsByEmailAndIdNot(dto.getEmail(), dto.getId())) {
            throw new DuplicateResourceException("user", "email", dto.getEmail());
        }

        User user = userRepository.findById(dto.getId())
                .orElseThrow(() -> new ResourceNotFoundException("user", "id", dto.getId()));

        normalizePasswordDates(dto);
        Set<Role> roles = resolveRoles(dto.getRoleIds());

        UserMapper.copyToExistingEntity(dto, user);
        user.setRoles(roles);

        if (StringUtils.hasText(dto.getPassword())) {
            user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        }

        userRepository.save(user);
    }

    /**
     * Elimina un usuario de forma veraz y con protecciones mínimas de administración.
     */
    @Override
    public void delete(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("user", "id", id));

        String currentAuthenticatedEmail = resolveCurrentAuthenticatedEmail();

        if (currentAuthenticatedEmail != null
                && currentAuthenticatedEmail.equalsIgnoreCase(user.getEmail())) {
            throw new IllegalStateException("msg.user-controller.delete.self");
        }

        if (hasRole(user, ROLE_ADMIN) && userRepository.countByRoles_Name(ROLE_ADMIN) <= 1) {
            throw new IllegalStateException("msg.user-controller.delete.lastAdmin");
        }

        userRepository.delete(user);
        userRepository.flush();

        if (userRepository.existsById(id)) {
            throw new IllegalStateException("msg.user-controller.delete.notDeleted");
        }
    }

    /**
     * Devuelve el detalle de un usuario.
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetailDTO getDetail(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("user", "id", id));
        return UserMapper.toDetailDTO(user);
    }

    /**
     * Rellena las fechas derivadas de contraseña en creación.
     */
    private void normalizePasswordDates(UserCreateDTO dto) {
        LocalDateTime lastPasswordChange = dto.getLastPasswordChange();
        if (lastPasswordChange == null) {
            lastPasswordChange = LocalDateTime.now();
            dto.setLastPasswordChange(lastPasswordChange);
        }
        dto.setPasswordExpiresAt(lastPasswordChange.plusDays(PASSWORD_EXPIRY_DAYS));
    }

    /**
     * Rellena las fechas derivadas de contraseña en edición.
     */
    private void normalizePasswordDates(UserUpdateDTO dto) {
        LocalDateTime lastPasswordChange = dto.getLastPasswordChange();
        if (lastPasswordChange == null) {
            lastPasswordChange = LocalDateTime.now();
            dto.setLastPasswordChange(lastPasswordChange);
        }
        dto.setPasswordExpiresAt(lastPasswordChange.plusDays(PASSWORD_EXPIRY_DAYS));
    }

    /**
     * Resuelve los roles desde sus ids.
     */
    private Set<Role> resolveRoles(Set<Long> roleIds) {
        Set<Long> ids = roleIds != null ? roleIds : Set.of();

        if (ids.isEmpty()) {
            return new HashSet<>();
        }

        Set<Role> roles = new HashSet<>(roleRepository.findAllById(ids));

        if (roles.size() != ids.size()) {
            Set<Long> foundIds = new HashSet<>();
            for (Role role : roles) {
                if (role != null && role.getId() != null) {
                    foundIds.add(role.getId());
                }
            }

            for (Long requestedId : ids) {
                if (!foundIds.contains(requestedId)) {
                    throw new ResourceNotFoundException("role", "id", requestedId);
                }
            }
        }

        return roles;
    }

    /**
     * Devuelve el email del usuario autenticado actual.
     */
    private String resolveCurrentAuthenticatedEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        String name = authentication.getName();
        return "anonymousUser".equalsIgnoreCase(name) ? null : name;
    }

    /**
     * Comprueba si el usuario tiene un rol concreto.
     */
    private boolean hasRole(User user, String roleName) {
        return user != null
                && user.getRoles() != null
                && user.getRoles().stream()
                .anyMatch(role -> role != null && roleName.equalsIgnoreCase(role.getName()));
    }
}