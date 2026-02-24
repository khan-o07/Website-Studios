package com.websitestudios.exception;

/**
 * Thrown when an authenticated user lacks permission for the requested action.
 * Maps to HTTP 403 Forbidden.
 *
 * Will be actively used in Phase 6 (RBAC).
 *
 * Usage:
 * throw new ForbiddenException("You do not have permission to delete project
 * requests");
 */
public class ForbiddenException extends RuntimeException {

    private final String requiredRole;

    public ForbiddenException(String message) {
        super(message);
        this.requiredRole = null;
    }

    public ForbiddenException(String message, String requiredRole) {
        super(message);
        this.requiredRole = requiredRole;
    }

    public String getRequiredRole() {
        return requiredRole;
    }
}