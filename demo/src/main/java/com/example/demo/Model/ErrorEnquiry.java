package com.example.demo.Model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

/**
 * Table ErrorEnquiry - the audit trail the project requires for runtime errors.
 *
 * <p>Rows are written by {@code ErrorLogService} from the global exception handler and the
 * authentication failure listener, so every uncaught failure leaves a record behind.
 */
@Entity
@Table(name = "ErrorEnquiry")
public class ErrorEnquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "errorTitle", nullable = false, length = 255)
    private String errorTitle;

    @Lob
    @Column(name = "errorLog_Message", nullable = false)
    private String errorLogMessage;

    @Lob
    @Column(name = "describetionAboutErrors")
    private String describetionAboutErrors;

    @Lob
    @Column(name = "issueFixedInfo")
    private String issueFixedInfo;

    @Column(name = "createdAt", nullable = false)
    private Instant createdAt;

    public ErrorEnquiry() {
    }

    public ErrorEnquiry(String errorTitle, String errorLogMessage, String describetionAboutErrors,
            String issueFixedInfo, Instant createdAt) {
        this.errorTitle = errorTitle;
        this.errorLogMessage = errorLogMessage;
        this.describetionAboutErrors = describetionAboutErrors;
        this.issueFixedInfo = issueFixedInfo;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getErrorTitle() {
        return errorTitle;
    }

    public void setErrorTitle(String errorTitle) {
        this.errorTitle = errorTitle;
    }

    public String getErrorLogMessage() {
        return errorLogMessage;
    }

    public void setErrorLogMessage(String errorLogMessage) {
        this.errorLogMessage = errorLogMessage;
    }

    public String getDescribetionAboutErrors() {
        return describetionAboutErrors;
    }

    public void setDescribetionAboutErrors(String describetionAboutErrors) {
        this.describetionAboutErrors = describetionAboutErrors;
    }

    public String getIssueFixedInfo() {
        return issueFixedInfo;
    }

    public void setIssueFixedInfo(String issueFixedInfo) {
        this.issueFixedInfo = issueFixedInfo;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
