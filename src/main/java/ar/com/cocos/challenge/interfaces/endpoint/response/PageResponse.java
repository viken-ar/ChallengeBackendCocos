package ar.com.cocos.challenge.interfaces.endpoint.response;

import java.util.List;

/**
 * Response de paginación genérico reutilizable.
 * Contiene el contenido de la página actual y los metadatos de paginación.
 */
public record PageResponse<T>(
    List<T> content,
    boolean isFirst,
    boolean isLast,
    long totalElements,
    int totalPages
) {}
