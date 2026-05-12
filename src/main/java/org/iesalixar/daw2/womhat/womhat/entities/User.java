package org.iesalixar.daw2.womhat.womhat.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidad JPA para la tabla 'users'.
 *
 * - Credenciales: email + password_hash
 * - Estado: active, account_non_locked, etc.
 * - Relación 1:1 con UserProfile (PK compartida, tabla user_profiles).
 * - Relación N:M con Role (tabla puente user_roles).
 * - Relación 1:N con PasswordResetToken (un usuario puede generar varios tokens).
 * - Relación 1:N con UserRackAccess (permisos a racks).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"profile", "roles", "resetTokens", "rackAccess"})
@Entity
@Table(
        name = "users",
        uniqueConstraints = @UniqueConstraint(name = "uq_users_email", columnNames = "email")
)
public class User {

    /** BIGINT AUTO_INCREMENT PRIMARY KEY */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** VARCHAR(100) NOT NULL UNIQUE */
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    /** VARCHAR(500) NOT NULL */
    @Column(name = "password_hash", nullable = false, length = 500)
    private String passwordHash;

    /** BOOLEAN NOT NULL DEFAULT TRUE */
    @Column(name = "active", nullable = false)
    private boolean active = true;

    /** BOOLEAN NOT NULL DEFAULT TRUE */
    @Column(name = "account_non_locked", nullable = false)
    private boolean accountNonLocked = true;

    /** DATETIME NULL */
    @Column(name = "last_password_change")
    private LocalDateTime lastPasswordChange;

    /** DATETIME NULL */
    @Column(name = "password_expires_at")
    private LocalDateTime passwordExpiresAt;

    /** INT NOT NULL DEFAULT 0 */
    @Column(name = "failed_login_attempts", nullable = false)
    private Integer failedLoginAttempts = 0;

    /** BOOLEAN NOT NULL DEFAULT FALSE */
    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    /** BOOLEAN NOT NULL DEFAULT FALSE */
    @Column(name = "must_change_password", nullable = false)
    private boolean mustChangePassword = false;

    /**
     * Relación 1:1 con perfil (lado NO propietario).
     * El FK real está en user_profiles.user_id.
     */
    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private UserProfile profile;

    /**
     * Relación N:M con roles mediante user_roles.
     * Este lado es el propietario del join.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id")
    )
    private Set<Role> roles = new HashSet<>();

    /** 1 usuario -> N tokens */
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private Set<PasswordResetToken> resetTokens = new HashSet<>();

    /** 1 usuario -> N accesos a racks */
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private Set<UserRackAccess> rackAccess = new HashSet<>();
}
