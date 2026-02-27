package com.websitestudios.ratelimit;

import com.websitestudios.exception.DuplicateRequestException;
import com.websitestudios.repository.ProjectRequestRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Throttles duplicate form submissions based on email + phone hash combination.
 *
 * This is SEPARATE from the hard duplicate check in the service layer:
 *
 * DuplicateRequestThrottler → Time-based: "same contact submitted < 10 min ago"
 * Service.createProjectRequest → Hash-based: "exact same email+phone already
 * exists"
 *
 * The throttler provides a user-friendly cooldown period, while the
 * service-level
 * check provides a hard permanent duplicate block.
 *
 * Called from ProjectRequestServiceImpl BEFORE the hard duplicate check.
 */
@Component
public class DuplicateRequestThrottler {

    private static final Logger log = LoggerFactory.getLogger(DuplicateRequestThrottler.class);

    private final ProjectRequestRepository projectRequestRepository;
    private final RateLimitProperties rateLimitProperties;

    public DuplicateRequestThrottler(ProjectRequestRepository projectRequestRepository,
            RateLimitProperties rateLimitProperties) {
        this.projectRequestRepository = projectRequestRepository;
        this.rateLimitProperties = rateLimitProperties;
    }

    /**
     * Check if a submission from this email+phone combination was made
     * within the cooldown window.
     *
     * @param emailHash SHA-256 hash of the email
     * @param phoneHash SHA-256 hash of the phone number
     * @throws DuplicateRequestException if within cooldown period
     */
    public void checkCooldown(String emailHash, String phoneHash) {

        int cooldownMinutes = rateLimitProperties.getSameContactCooldownMinutes();
        Instant cooldownStart = Instant.now().minusSeconds(cooldownMinutes * 60L);

        boolean recentSubmissionExists = projectRequestRepository
                .existsByEmailHashAndPhoneHashAndIsDeletedFalseAndCreatedAtAfter(
                        emailHash, phoneHash, cooldownStart);

        if (recentSubmissionExists) {
            log.warn("Duplicate submission throttled — same contact within {}min cooldown. " +
                    "email_hash: {}..., phone_hash: {}...",
                    cooldownMinutes,
                    emailHash.substring(0, 8),
                    phoneHash.substring(0, 8));

            throw new DuplicateRequestException(
                    "A project request from this contact was submitted recently. " +
                            "Please wait " + cooldownMinutes + " minutes before submitting again.");
        }

        log.debug("Cooldown check passed for email_hash: {}...", emailHash.substring(0, 8));
    }
}