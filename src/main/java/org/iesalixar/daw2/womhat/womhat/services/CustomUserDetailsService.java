package org.iesalixar.daw2.womhat.womhat.services;

import lombok.RequiredArgsConstructor;
import org.iesalixar.daw2.womhat.womhat.entities.User;
import org.iesalixar.daw2.womhat.womhat.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

/**
 * Servicio de Spring Security que carga usuarios desde base de datos.
 *
 * En este proyecto el identificador de login es el email.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(CustomUserDetailsService.class);

    @Autowired
    private UserRepository userRepository;

    /**
     * Carga un usuario por email.
     *
     * @param username email introducido en login
     * @return UserDetails de Spring Security
     * @throws UsernameNotFoundException si no existe
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Entrando en loadUserByUsername(username={})", username);

        final String email = username;

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> {
                    log.warn("No se encontró usuario con email={}", email);
                    return new UsernameNotFoundException("Usuario no encontrado: " + email);
                });

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPasswordHash())
                .authorities(
                        user.getRoles().stream()
                                .map(role -> role.getName())
                                .collect(Collectors.toList())
                                .toArray(new String[0])
                )
                .accountExpired(false)
                .accountLocked(!user.isAccountNonLocked())
                .credentialsExpired(false)
                .disabled(!user.isActive())
                .build();

        log.debug("Saliendo de loadUserByUsername(email={})", email);

        return userDetails;
    }
}
