package org.iesalixar.daw2.womhat.womhat.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Entidad JPA para la tabla 'roles'.
 *
 * - Un rol puede estar asignado a muchos usuarios (N:M).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = "users")
@Entity
@Table(
        name = "roles",
        uniqueConstraints = @UniqueConstraint(name = "uq_roles_name", columnNames = "name")
)
public class Role {

    /** BIGINT AUTO_INCREMENT PRIMARY KEY */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** VARCHAR(50) NOT NULL UNIQUE (ROLE_ADMIN, ROLE_USER...) */
    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    /** VARCHAR(100) NOT NULL */
    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    /** VARCHAR(255) NULL */
    @Column(name = "description", length = 255)
    private String description;

    /** Lado NO propietario del N:M */
    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    private Set<User> users = new HashSet<>();
}
