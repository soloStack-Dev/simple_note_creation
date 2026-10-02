package com.example.demo.Service;

import java.time.Clock;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.Model.ErrorEnquiry;
import com.example.demo.Repository.ErrorEnquiryRepository;

/**
 * Writes every runtime error the application hits into the ErrorEnquiry table.
 *
 * <p>Logging is deliberately defensive: it runs {@link Propagation#REQUIRES_NEW} so a row can
 * still be written while handling a failure that happened inside another transaction, and
 * every secondary failure is swallowed. A broken audit log must never turn a handled error
 * into a 500.
 */
@Service
public class ErrorLogService {

    private static final Logger log = LoggerFactory.getLogger(ErrorLogService.class);

    private static final int MAX_FIELD_LENGTH = 4000;

    private final ErrorEnquiryRepository repository;
    private final Clock clock;

    public ErrorLogService(ErrorEnquiryRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(String title, String errorLogMessage, String description, String fixInfo) {
        try {
            repository.save(new ErrorEnquiry(
                    truncate(title, 255),
                    truncate(errorLogMessage, MAX_FIELD_LENGTH),
                    truncate(description, MAX_FIELD_LENGTH),
                    truncate(fixInfo, MAX_FIELD_LENGTH),
                    Instant.now(clock)));
        } catch (Exception ex) {
            log.warn("Could not persist error entry '{}': {}", title, ex.toString());
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordException(String title, Throwable throwable, String fixInfo) {
        record(title,
                throwable == null ? "unknown" : throwable.toString(),
                describe(throwable),
                fixInfo);
    }

    private String describe(Throwable throwable) {
        if (throwable == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        Throwable current = throwable;
        int depth = 0;
        while (current != null && depth < 5) {
            if (depth > 0) {
                sb.append("Caused by: ");
            }
            sb.append(current.getClass().getName());
            if (current.getMessage() != null) {
                sb.append(": ").append(current.getMessage());
            }
            current = current.getCause();
            depth++;
        }
        return sb.toString();
    }

    private String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }
}
