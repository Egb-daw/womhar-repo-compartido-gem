package org.iesalixar.daw2.womhat.womhat.handlers;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.iesalixar.daw2.womhat.womhat.entities.Role;
import org.iesalixar.daw2.womhat.womhat.entities.User;
import org.iesalixar.daw2.womhat.womhat.entities.UserProfile;
import org.iesalixar.daw2.womhat.womhat.repositories.RoleRepository;
import org.iesalixar.daw2.womhat.womhat.repositories.UserProfileRepository;
import org.iesalixar.daw2.womhat.womhat.repositories.UserRepository;
import org.iesalixar.daw2.womhat.womhat.services.CustomUserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * Handler personalizado para manejar eventos de éxito en la autenticación con OAuth2.
 * Este handler verifica si el usuario autenticado con un proveedor externo (por ejemplo, GitHub)
 * ya está registrado en la base de datos de la aplicación. Si no está registrado, se redirige
 * al usuario a una página de registro. En caso contrario, el usuario es redirigido a la página principal.
 */
@Component
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(CustomOAuth2SuccessHandler.class);
    private static final String ROLE_USER = "ROLE_USER";
    private static final String OAUTH2_ERROR_EMAIL_MISSING = "github_email_missing";
    private static final String OAUTH2_ERROR_USER_BLOCKED = "github_user_blocked";
    private static final int PASSWORD_EXPIRY_DAYS = 90;
    private static final BCryptPasswordEncoder OAUTH_PASSWORD_ENCODER = new BCryptPasswordEncoder();
    private static final RequestCache REQUEST_CACHE = new HttpSessionRequestCache();

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserProfileRepository userProfileRepository;
    private final CustomUserDetailsService customUserDetailsService;

    /**
     * Maneja el evento de autenticación exitosa con OAuth2.
     * Verifica si el usuario autenticado ya existe en la base de datos de la aplicación.
     * Si no existe, se redirige a una página de registro. Si existe, se redirige al inicio.
     *
     * @param request       Objeto {@link HttpServletRequest} que contiene la solicitud HTTP.
     * @param response      Objeto {@link HttpServletResponse} que contiene la respuesta HTTP.
     * @param authentication Objeto {@link Authentication} que representa al usuario autenticado.
     * @throws IOException      Si ocurre un error en la redirección.
     * @throws ServletException Si ocurre un error en el manejo de la solicitud.
     */
    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = resolveGithubEmail(oAuth2User);
        String login = oAuth2User.getAttribute("login");

        if (!StringUtils.hasText(email)) {
            logger.warn("Login OAuth2 rechazado: GitHub no devolvió email utilizable para login={}", login);
            throw new OAuth2AuthenticationException(
                    new OAuth2Error(OAUTH2_ERROR_EMAIL_MISSING),
                    "GitHub no devolvió un email utilizable."
            );
        }

        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

        User appUser = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .map(existing -> {
                    logger.info("Login OAuth2 enlazado con usuario existente {}", existing.getEmail());
                    return existing;
                })
                .orElseGet(() -> createOAuthUser(normalizedEmail, oAuth2User));

        appUser = ensureUserHasAtLeastRoleUser(appUser);

        if (!appUser.isActive() || !appUser.isAccountNonLocked()) {
            logger.warn("Login OAuth2 denegado para {}: cuenta inactiva o bloqueada.", appUser.getEmail());
            throw new OAuth2AuthenticationException(
                    new OAuth2Error(OAUTH2_ERROR_USER_BLOCKED),
                    "La cuenta está deshabilitada o bloqueada."
            );
        }

        ensureProfile(appUser, oAuth2User);

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(appUser.getEmail());

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );
        authenticationToken.setDetails(authentication.getDetails());

        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        logger.info("Login OAuth2 completado para {}", appUser.getEmail());
        SavedRequest savedRequest = REQUEST_CACHE.getRequest(request, response);
        if (savedRequest != null && StringUtils.hasText(savedRequest.getRedirectUrl())) {
            REQUEST_CACHE.removeRequest(request, response);
            response.sendRedirect(savedRequest.getRedirectUrl());
            return;
        }
        response.sendRedirect(request.getContextPath() + "/dashboard");
    }

    /**
     * Extrae el email devuelto por GitHub en el perfil OAuth2.
     * Si no llega email usable, el flujo debe abortarse con mensaje controlado.
     */
    private String resolveGithubEmail(OAuth2User oAuth2User) {
        if (oAuth2User == null) {
            return null;
        }
        String email = oAuth2User.getAttribute("email");
        return StringUtils.hasText(email) ? email.trim() : null;
    }

    private User createOAuthUser(String normalizedEmail, OAuth2User oAuth2User) {
        Role userRole = roleRepository.findByNameIgnoreCase(ROLE_USER)
                .orElseThrow(() -> new IllegalStateException("No existe el rol ROLE_USER para alta OAuth2."));

        LocalDateTime now = LocalDateTime.now();
        User user = new User();
        user.setEmail(normalizedEmail);
        user.setPasswordHash(OAUTH_PASSWORD_ENCODER.encode(UUID.randomUUID().toString()));
        user.setActive(true);
        user.setAccountNonLocked(true);
        user.setLastPasswordChange(now);
        user.setPasswordExpiresAt(now.plusDays(PASSWORD_EXPIRY_DAYS));
        user.setFailedLoginAttempts(0);
        user.setEmailVerified(true);
        user.setMustChangePassword(false);

        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        user.setRoles(roles);

        try {
            User saved = userRepository.save(user);
            logger.info("Alta automática OAuth2 de nuevo usuario ROLE_USER: {}", saved.getEmail());
            return saved;
        } catch (DataIntegrityViolationException ex) {
            logger.info("Alta OAuth2 concurrente detectada para {}. Se reutiliza usuario existente.", normalizedEmail);
            return userRepository.findByEmailIgnoreCase(normalizedEmail)
                    .orElseThrow(() -> ex);
        }
    }

    private User ensureUserHasAtLeastRoleUser(User user) {
        if (user == null) {
            return null;
        }

        boolean alreadyHasRoles = user.getRoles() != null && !user.getRoles().isEmpty();
        if (alreadyHasRoles) {
            return user;
        }

        Role userRole = roleRepository.findByNameIgnoreCase(ROLE_USER)
                .orElseThrow(() -> new IllegalStateException("No existe el rol ROLE_USER para enlazar cuenta OAuth2."));

        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        user.setRoles(roles);

        User saved = userRepository.save(user);
        if (saved.getId() != null && userRole.getId() != null) {
            userRepository.ensureUserRole(saved.getId(), userRole.getId());
        }
        logger.warn("Cuenta OAuth2 sin roles detectada para {}. Se asigna ROLE_USER automáticamente.", saved.getEmail());
        return saved;
    }

    private void ensureProfile(User user, OAuth2User oAuth2User) {
        if (user == null || user.getId() == null || userProfileRepository.existsByUserId(user.getId())) {
            return;
        }

        UserProfile profile = new UserProfile();
        profile.setUser(user);
        profile.setFirstName(resolveFirstName(oAuth2User));
        profile.setLastName("");
        profile.setPhoneNumber(null);
        profile.setProfileImage(null);
        profile.setBio(null);
        profile.setLocale("es");
        userProfileRepository.save(profile);
    }

    private String resolveFirstName(OAuth2User oAuth2User) {
        String fullName = oAuth2User != null ? oAuth2User.getAttribute("name") : null;
        if (StringUtils.hasText(fullName)) {
            String candidate = fullName.trim();
            return candidate.length() > 60 ? candidate.substring(0, 60) : candidate;
        }

        String login = oAuth2User != null ? oAuth2User.getAttribute("login") : null;
        if (StringUtils.hasText(login)) {
            String candidate = login.trim();
            return candidate.length() > 60 ? candidate.substring(0, 60) : candidate;
        }

        return "GitHub";
    }
}
