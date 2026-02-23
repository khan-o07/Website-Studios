package com.websitestudios.util;

/**
 * Application-wide constants for Website Studios.
 * Centralized location to avoid magic strings throughout the codebase.
 */
public final class WsConstants {

    private WsConstants() {
        throw new UnsupportedOperationException(
                "WsConstants is a utility class and cannot be instantiated");
    }

    // ─── API ───
    public static final String API_BASE_PATH = "/api";
    public static final String API_VERSION = "/v1";
    public static final String API_V1_PATH = API_BASE_PATH + API_VERSION;

    // ─── Endpoints ───
    public static final String PROJECT_REQUESTS_PATH = API_V1_PATH + "/project-requests";
    public static final String COUNTRY_CODES_PATH = API_V1_PATH + "/country-codes";
    public static final String AUTH_PATH = API_V1_PATH + "/auth";
    public static final String ADMIN_PATH = API_V1_PATH + "/admin";
    public static final String HEALTH_PATH = API_V1_PATH + "/health";

    // ─── Security ───
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final int BCRYPT_STRENGTH = 12;

    // ─── Pagination Defaults ───
    public static final String DEFAULT_PAGE = "0";
    public static final String DEFAULT_PAGE_SIZE = "20";
    public static final int MAX_PAGE_SIZE = 100;
    public static final String DEFAULT_SORT_FIELD = "createdAt";
    public static final String DEFAULT_SORT_DIRECTION = "desc";

    // ─── Validation ───
    public static final int NAME_MIN_LENGTH = 2;
    public static final int NAME_MAX_LENGTH = 150;
    public static final int PHONE_MIN_LENGTH = 6;
    public static final int PHONE_MAX_LENGTH = 15;
    public static final int EMAIL_MAX_LENGTH = 255;
    public static final String PHONE_REGEX = "^[0-9]{6,15}$";
    public static final String COUNTRY_CODE_REGEX = "^\\+\\d{1,4}$";

    // ─── Project Types (3 core offerings) ───
    public static final int TOTAL_PROJECT_TYPES = 3;
    public static final String VALID_PROJECT_TYPES_MSG = "Valid project types are: ANDROID_APP, IOS_APP, WEBSITE";

    // ─── Entities ───
    public static final String ENTITY_PROJECT_REQUEST = "ProjectRequest";
    public static final String ENTITY_STUDIO_ADMIN = "StudioAdmin";

    // ─── Messages ───
    public static final String MSG_REQUEST_SUBMITTED = "Project request submitted successfully";
    public static final String MSG_REQUEST_NOT_FOUND = "Project request not found";
    public static final String MSG_DUPLICATE_REQUEST = "A request with this email and phone already exists";
    public static final String MSG_STATUS_UPDATED = "Project status updated successfully";
    public static final String MSG_REQUEST_DELETED = "Project request deleted successfully";
    public static final String MSG_CAPTCHA_FAILED = "reCAPTCHA verification failed";
    public static final String MSG_RATE_LIMIT = "Too many requests. Please try again later";
    public static final String MSG_UNAUTHORIZED = "Authentication required";
    public static final String MSG_FORBIDDEN = "You don't have permission to perform this action";
    public static final String MSG_ACCOUNT_LOCKED = "Account is temporarily locked due to multiple failed attempts";
    public static final String MSG_INVALID_CREDENTIALS = "Invalid username or password";
    public static final String MSG_INVALID_PROJECT_TYPE = "Invalid project type. " + VALID_PROJECT_TYPES_MSG;

    // ─── Masking ───
    public static final String EMAIL_MASK_PATTERN = "(^[^@]{2})[^@]*(@.*)";
    public static final String EMAIL_MASK_REPLACEMENT = "$1***$2";
    public static final int PHONE_VISIBLE_LAST_DIGITS = 4;
}