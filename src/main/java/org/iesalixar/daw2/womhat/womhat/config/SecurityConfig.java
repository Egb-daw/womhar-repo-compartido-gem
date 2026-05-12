package org.iesalixar.daw2.womhat.womhat.config;

import org.iesalixar.daw2.womhat.womhat.handlers.CustomOAuth2FailureHandler;
import org.iesalixar.daw2.womhat.womhat.handlers.CustomOAuth2SuccessHandler;
import org.iesalixar.daw2.womhat.womhat.services.CustomUserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configura la seguridad de la aplicación, definiendo autenticación y autorización
 * para diferentes roles de usuario, y gestionando la política de sesiones.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // Activa la seguridad basada en métodos
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private CustomOAuth2SuccessHandler customOAuth2SuccessHandler;

    @Autowired
    private CustomOAuth2FailureHandler customOAuth2FailureHandler;

    /**
     * Configura el filtro de seguridad para las solicitudes HTTP, especificando las
     * rutas permitidas y los roles necesarios para acceder a diferentes endpoints.
     *
     * @param http instancia de {@link HttpSecurity} para configurar la seguridad.
     * @return una instancia de {@link SecurityFilterChain} que contiene la configuración de seguridad.
     * @throws Exception si ocurre un error en la configuración de seguridad.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        logger.info("Entrando en el método securityFilterChain");

        // Configuración de seguridad
        http
                .authorizeHttpRequests(auth -> {
                    logger.debug("Configurando autorización de solicitudes HTTP");

                    auth
                            // Público real
                            .requestMatchers(
                                    "/",
                                    "/.well-known/**",
                                    "/assets/**",
                                    "/js/**",
                                    "/css/**",
                                    "/images/**",
                                    "/webjars/**",
                                    "/login",
                                    "/register",
                                    "/contact",
                                    "/about",
                                    "/auth/forgot-password",
                                    "/auth/forgot",
                                    "/auth/reset-password",
                                    "/oauth2/**",
                                    "/login/oauth2/**",
                                    "/error",
                                    "/error/**"
                            ).permitAll()

                            // Backoffice exclusivo de ADMIN
                            .requestMatchers(
                                    "/users",
                                    "/users/**",
                                    "/data-centers",
                                    "/data-centers/**",
                                    "/rooms",
                                    "/rooms/**",
                                    "/racks/new",
                                    "/racks/edit",
                                    "/racks/insert",
                                    "/racks/update",
                                    "/racks/delete",
                                    "/equipment/delete",
                                    "/catalog/orders/fulfill"
                            ).hasRole("ADMIN")

                            // Zona privada común
                            .requestMatchers(
                                    "/dashboard",
                                    "/profile",
                                    "/profile/**",
                                    "/catalog",
                                    "/catalog/detail",
                                    "/catalog/orders",
                                    "/catalog/orders/cancel",
                                    "/maintenance/work-orders",
                                    "/maintenance/work-orders/**",
                                    "/maintenance-notes/**"
                            ).hasAnyRole("ADMIN", "USER")

                            // Lectura técnica permitida para usuarios con acceso filtrado por servicio
                            .requestMatchers(
                                    HttpMethod.GET,
                                    "/racks",
                                    "/racks/detail",
                                    "/racks/report",
                                    "/equipment",
                                    "/equipment/detail",
                                    "/equipment/qr-card",
                                    "/equipment/qr.png"
                            ).hasAnyRole("ADMIN", "USER")

                            // Todo lo demás requiere login
                            .anyRequest().authenticated();
                })
                .formLogin(form -> {
                    logger.debug("Configurando formulario de inicio de sesión");
                    form
                            .loginPage("/login")
                            .loginProcessingUrl("/login")
                            .usernameParameter("username")
                            .passwordParameter("password")
                            .defaultSuccessUrl("/dashboard", false)
                            .failureUrl("/login?error")
                            .permitAll();
                })
                .logout(logout -> {
                    logger.debug("Configurando el cierre de sesión");
                    logout
                            .logoutUrl("/logout")
                            .logoutSuccessUrl("/login?logout") // Mensaje de éxito al salir
                            .permitAll();
                })
                .exceptionHandling(ex -> ex
                        .accessDeniedPage("/error/403")
                )
                .oauth2Login(oauth2 -> {
                    logger.debug("Configurando login con OAuth2");
                    oauth2
                            .loginPage("/login")        // Reutiliza la página de inicio de sesión personalizada
                            .successHandler(customOAuth2SuccessHandler) // Usa el Success Handler personalizado
                            .failureHandler(customOAuth2FailureHandler); // Handler para fallo en autenticación
                })
                .sessionManagement(session -> {
                    logger.debug("Configurando política de gestión de sesiones");
                    // Usa sesiones cuando sea necesario
                    session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED);
                });

        logger.info("Saliendo del método securityFilterChain");
        return http.build();
    }

    /**
     * Provider de autenticación basado en DAO.
     *
     * <p>Usa el {@link CustomUserDetailsService} para localizar usuarios en BD y el
     * {@link PasswordEncoder} para verificar la contraseña (BCrypt).</p>
     *
     * @return {@link DaoAuthenticationProvider} configurado.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        logger.info("Entrando en el método authenticationProvider");

        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());

        logger.info("Saliendo del método authenticationProvider");
        return provider;
    }

    /**
     * Configura el codificador de contraseñas para cifrar las contraseñas de los usuarios
     * utilizando BCrypt.
     *
     * @return una instancia de {@link PasswordEncoder} que utiliza BCrypt para cifrar contraseñas.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        logger.info("Entrando en el método passwordEncoder");
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        logger.info("Saliendo del método passwordEncoder");
        return encoder;
    }
}
