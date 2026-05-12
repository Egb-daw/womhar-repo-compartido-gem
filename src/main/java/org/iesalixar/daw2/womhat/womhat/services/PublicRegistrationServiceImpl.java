package org.iesalixar.daw2.womhat.womhat.services;

import org.iesalixar.daw2.womhat.womhat.dtos.PublicRegisterDTO;
import org.iesalixar.daw2.womhat.womhat.entities.Role;
import org.iesalixar.daw2.womhat.womhat.entities.User;
import org.iesalixar.daw2.womhat.womhat.entities.UserProfile;
import org.iesalixar.daw2.womhat.womhat.exceptions.DuplicateResourceException;
import org.iesalixar.daw2.womhat.womhat.exceptions.ResourceNotFoundException;
import org.iesalixar.daw2.womhat.womhat.repositories.RoleRepository;
import org.iesalixar.daw2.womhat.womhat.repositories.UserProfileRepository;
import org.iesalixar.daw2.womhat.womhat.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Implementación del registro público.
 *
 * Decisiones:
 * - crea usuario real en BD
 * - asigna ROLE_USER
 * - crea perfil mínimo para no dejar el 1:1 roto
 * - no toca el diseño visual del formulario
 */
@Service
@Transactional
public class PublicRegistrationServiceImpl implements PublicRegistrationService {

    private static final Logger logger = LoggerFactory.getLogger(PublicRegistrationServiceImpl.class);
    private static final int PASSWORD_EXPIRY_DAYS = 90;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Registra un usuario nuevo desde el formulario público.
     */
    @Override
    public void register(PublicRegisterDTO dto) {
        String normalizedEmail = dto.getEmail().trim().toLowerCase();
        String displayName = dto.getUsername().trim();

        logger.info("Intentando registrar usuario público con email={}", normalizedEmail);

        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new DuplicateResourceException("user", "email", normalizedEmail);
        }

        Role userRole = roleRepository.findByNameIgnoreCase("ROLE_USER")
                .orElseThrow(() -> new ResourceNotFoundException("role", "name", "ROLE_USER"));

        LocalDateTime now = LocalDateTime.now();

        User user = new User();
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setActive(true);
        user.setAccountNonLocked(true);
        user.setLastPasswordChange(now);
        user.setPasswordExpiresAt(now.plusDays(PASSWORD_EXPIRY_DAYS));
        user.setFailedLoginAttempts(0);
        user.setEmailVerified(false);
        user.setMustChangePassword(false);

        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        user.setRoles(roles);

        User savedUser = userRepository.save(user);

        UserProfile profile = new UserProfile();
        profile.setUser(savedUser);

        /*
         * Como el formulario visual actual solo tiene username/email/password,
         * usamos username como nombre visible del perfil.
         */
        profile.setFirstName(displayName);
        profile.setLastName("");
        profile.setPhoneNumber(null);
        profile.setProfileImage(null);
        profile.setBio(null);
        profile.setLocale("es");

        userProfileRepository.save(profile);

        logger.info("Usuario público registrado correctamente con id={}", savedUser.getId());
    }
}