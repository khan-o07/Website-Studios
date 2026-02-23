package com.websitestudios.enums;

/**
 * Represents the lifecycle status of a project request.
 * Follows: PENDING → REVIEWING → IN_PROGRESS → COMPLETED
 * or PENDING → CANCELLED at any stage.
 */
public enum ProjectStatusEnum {

    PENDING("Pending Review"),
    REVIEWING("Under Review"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled");

    private final String displayName;

    ProjectStatusEnum(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}