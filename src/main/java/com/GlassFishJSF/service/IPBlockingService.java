package com.GlassFishJSF.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

@Named
@ApplicationScoped
public class IPBlockingService implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final int MAX_FAILED_ATTEMPTS = 10;
    private static final int BLOCK_DURATION_MINUTES = 2;
    private static final Logger LOGGER = Logger.getLogger("SECURITY");

    private final Map<String, FailedAttempt> failedAttempts = new ConcurrentHashMap<>();

    public void recordFailedAttempt(String ip) {
        FailedAttempt attempt = failedAttempts.computeIfAbsent(ip, k -> new FailedAttempt());
        attempt.increment();

        // Log si pattern suspect détecté
        if (attempt.getCount() == 2) {
            LOGGER.warning(String.format("[CONNEXION] SUSPICIOUS_PATTERN | IP: %s | Timestamp: %s | Attempts: %d | Status: WATCH",
                    ip, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), attempt.getCount()));
        }
    }

    public void resetFailedAttempts(String ip) {
        if (failedAttempts.containsKey(ip)) {
            LOGGER.info(String.format("[CONNEXION] RESET_ATTEMPTS | IP: %s | Timestamp: %s | Reason: SUCCESSFUL_LOGIN",
                    ip, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
        }
        failedAttempts.remove(ip);
    }

    public boolean isBlocked(String ip) {
        FailedAttempt attempt = failedAttempts.get(ip);
        if (attempt == null) {
            return false;
        }

        if (attempt.isExpired()) {
            LOGGER.info(String.format("[CONNEXION] UNBLOCK_IP | IP: %s | Timestamp: %s | Reason: TIMEOUT_EXPIRED",
                    ip, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
            failedAttempts.remove(ip);
            return false;
        }

        return attempt.getCount() >= MAX_FAILED_ATTEMPTS;
    }

    public int getRemainingAttempts(String ip) {
        FailedAttempt attempt = failedAttempts.get(ip);
        if (attempt == null) {
            return MAX_FAILED_ATTEMPTS;
        }

        return Math.max(0, MAX_FAILED_ATTEMPTS - attempt.getCount());
    }

    private static class FailedAttempt {
        private int count = 0;
        private LocalDateTime blockTime;

        public void increment() {
            count++;
            if (count >= MAX_FAILED_ATTEMPTS) {
                blockTime = LocalDateTime.now();
            }
        }

        public int getCount() {
            return count;
        }

        public boolean isExpired() {
            if (blockTime == null) {
                return false;
            }
            return LocalDateTime.now().isAfter(blockTime.plusMinutes(BLOCK_DURATION_MINUTES));
        }
    }
}