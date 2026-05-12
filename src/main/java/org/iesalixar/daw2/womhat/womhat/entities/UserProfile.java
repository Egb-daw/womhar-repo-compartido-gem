package org.iesalixar.daw2.womhat.womhat.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidad JPA para la tabla 'user_profiles'.
 *
 * Relación 1:1 con 'users' mediante PRIMARY KEY compartida:
 *  - user_profiles.user_id es PK y también FK -> users.id
 *
 * Se modela con:
 *  - @MapsId para decir: "mi PK es el id del User asociado".
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = "user")
@Entity
@Table(name = "user_profiles")
public class UserProfile {

    /** PK compartida con users.id */
    @Id
    @Column(name = "user_id")
    private Long id;

    /**
     * 1:1 obligatorio en esta fila (si existe user_profiles, debe apuntar a un user).
     * @MapsId: id = user.id automáticamente.
     */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "first_name", nullable = false, length = 60)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 80)
    private String lastName;

    @Column(name = "phone_number", length = 30)
    private String phoneNumber;

    @Column(name = "profile_image", length = 255)
    private String profileImage;

    @Column(name = "bio", length = 500)
    private String bio;

    @Column(name = "locale", length = 10)
    private String locale;

    /**
     * DEFAULT CURRENT_TIMESTAMP en BD.
     * No lo insertamos/actualizamos desde Java.
     */
    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    /** DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP en BD. */
    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
