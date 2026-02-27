package com.websitestudios.monitoring;

import io.micrometer.core.instrument.Timer;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * AOP-based metrics integration.
 *
 * Intercepts service method calls and records timing metrics
 * without modifying business logic code.
 *
 * Instruments:
 * - Form submission flow (createProjectRequest)
 * - JWT validation (validateToken)
 */
@Aspect
@Component
public class MetricsIntegration {

    private final WsMetricsService wsMetricsService;

    public MetricsIntegration(WsMetricsService wsMetricsService) {
        this.wsMetricsService = wsMetricsService;
    }

    /**
     * Time the entire form submission pipeline.
     * Captures: captcha check + duplicate check + encryption + DB save
     */
    @Around("execution(* com.websitestudios.service.impl" +
            ".ProjectRequestServiceImpl.createProjectRequest(..))")
    public Object timeFormSubmission(ProceedingJoinPoint joinPoint) throws Throwable {

        Timer.Sample sample = wsMetricsService.startFormSubmissionTimer();
        wsMetricsService.incrementFormSubmitted();

        try {
            Object result = joinPoint.proceed();
            wsMetricsService.incrementFormSuccess();
            return result;

        } catch (Exception e) {
            // Identify metric by exception type
            String exceptionName = e.getClass().getSimpleName();

            if (exceptionName.contains("Duplicate")) {
                wsMetricsService.incrementFormDuplicate();
            } else if (exceptionName.contains("Captcha")) {
                wsMetricsService.incrementCaptchaFailed();
            }

            throw e; // Re-throw to normal exception handling

        } finally {
            wsMetricsService.stopFormSubmissionTimer(sample);
        }
    }

    /**
     * Time JWT token validation.
     */
    @Around("execution(* com.websitestudios.security.jwt" +
            ".JwtTokenProvider.validateToken(..))")
    public Object timeJwtValidation(ProceedingJoinPoint joinPoint) throws Throwable {

        Timer.Sample sample = wsMetricsService.startJwtValidationTimer();

        try {
            Object result = joinPoint.proceed();

            if (result instanceof Boolean && !(Boolean) result) {
                wsMetricsService.incrementInvalidToken();
            }

            return result;

        } catch (Exception e) {
            wsMetricsService.incrementInvalidToken();
            throw e;
        } finally {
            wsMetricsService.stopJwtValidationTimer(sample);
        }
    }
}