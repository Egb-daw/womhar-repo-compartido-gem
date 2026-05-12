package org.iesalixar.daw2.womhat.womhat.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidad JPA para 'password_reset_tokens'.
 *
 * - Un usuario puede generar varios tokens (N:1).
 * - Se almacena el hash del token, no el token en claro.
 * - created_at lo gestiona la BD (DEFAULT CURRENT_TIMESTAMP).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = "user")
@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken {

    /** BIGINT AUTO_INCREMENT PRIMARY KEY */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** FK NOT NULL -> users.id */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** VARCHAR(64) NOT NULL (SHA-256) */
    @Column(name = "token_hash", nullable = false, length = 64)
    private String tokenHash;

    /** DATETIME NOT NULL */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /** DATETIME NULL */
    @Column(name = "used_at")
    private LocalDateTime usedAt;

    /** DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP */
    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    /** VARCHAR(45) NULL */
    @Column(name = "request_ip", length = 45)
    private String requestIp;

    /** VARCHAR(255) NULL */
    @Column(name = "user_agent", length = 255)
    private String userAgent;

    /** @return true si la fecha actual es posterior a expiresAt */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /** @return true si usedAt no es null */
    public boolean isUsed() {
        return usedAt != null;
    }
}
