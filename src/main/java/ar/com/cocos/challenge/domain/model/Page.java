package ar.com.cocos.challenge.domain.model;

import java.util.List;

/**
 * Response de paginación genérico reutilizable.
 * Contiene el contenido de la página actual y los metadatos de paginación.
 */
public record Page<T>(
    List<T> content,
    boolean isFirst,
    boolean isLast,
    long totalElements,
    int totalPages
) {}
