package org.iesalixar.daw2.womhat.womhat.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

/**
 * Clave compuesta (PK) para user_rack_access:
 *  - user_id
 *  - rack_id
 *
 * JPA necesita una clase Serializable para PK compuestas.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Embeddable
public class UserRackAccessId implements Serializable {

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "rack_id")
    private Long rackId;
}
