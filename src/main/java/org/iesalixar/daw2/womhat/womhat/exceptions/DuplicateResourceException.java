package org.iesalixar.daw2.womhat.womhat.exceptions;

import lombok.Getter;

/**
 * Excepción genérica que indica que ya existe un recurso con un valor que debería ser único.
 * Se utiliza típicamente en la capa de servicios cuando, antes de insertar o actualizar,
 * se comprueba que un campo (como {@code code} o {@code email}) ya está siendo usado por otra entidad.
 * Por ejemplo: intentar crear una región con {@code code="AND"} cuando ya existe.
 */
@Getter
public class DuplicateResourceException extends RuntimeException {
    /**
     * Nombre del recurso/entidad en el que se ha detectado el duplicado (por ejemplo: "region", "province", "user").
     */
    private final String resource;
    /**
     * Campo que debería ser único y ha provocado el conflicto por duplicidad (por ejemplo: "code", "email").
     */
    private final String field;
    /**
     * Valor concreto que está duplicado (por ejemplo: "AND", "admin@site.com").
     */
    private final Object value;
    /**
     * Construye una excepción indicando el recurso y el campo cuyo valor ya existe.
     *
     * @param resource nombre del recurso/entidad donde se detecta el duplicado (ej. {@code "region"}).
     * @param field    nombre del campo duplicado (ej. {@code "code"}).
     * @param value    valor que ya existe para ese campo (ej. {@code "AND"}).
     */
    public DuplicateResourceException(String resource, String field, Object value) {
        super("Duplicate " + resource + " (" + field + "=" + value + ")");
        this.resource = resource;
        this.field = field;
        this.value = value;
    }
}