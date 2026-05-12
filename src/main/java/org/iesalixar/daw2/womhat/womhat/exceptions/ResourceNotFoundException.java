package org.iesalixar.daw2.womhat.womhat.exceptions;

import lombok.Getter;

/**
 * Excepción genérica que indica que no se ha encontrado un recurso en la aplicación.
 * Se utiliza típicamente en la capa de servicios cuando, tras consultar el repositorio,
 * no existe una entidad con el valor buscado (por ejemplo, una región con id=5).
 */
@Getter
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Nombre del recurso/entidad que no se ha encontrado (por ejemplo: "region", "province", "user").
     */
    private final String resource;

    /**
     * Campo utilizado en la búsqueda que ha provocado el "no encontrado" (por ejemplo: "id", "code", "email").
     */
    private final String field;

    /**
     * Valor concreto del campo con el que se intentó localizar el recurso (por ejemplo: "5", "AND", "a@b.com").
     */
    private final Object value;

    /**
     * Construye una excepción indicando el recurso y el criterio de búsqueda que no ha devuelto resultados.
     *
     * @param resource nombre del recurso/entidad (ej. {@code "region"}).
     * @param field    nombre del campo usado como criterio de búsqueda (ej. {@code "id"}).
     * @param value    valor del campo usado en la búsqueda (ej. {@code 5}).
     */
    public ResourceNotFoundException(String resource, String field, Object value) {
        super(resource + " not found (" + field + "=" + value + ")");
        this.resource = resource;
        this.field = field;
        this.value = value;
    }
}
