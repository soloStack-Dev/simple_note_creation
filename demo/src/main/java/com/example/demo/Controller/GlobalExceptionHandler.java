package com.example.demo.Controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.example.demo.Service.ErrorLogService;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Turns uncaught failures into a rendered page and, more importantly, into an ErrorEnquiry row.
 *
 * <p>HTMX requests get a minimal alert fragment; browsers get the full error page. The page
 * name is passed in by the caller where it is known, so a failure inside the note grid can
 * still come back in the modal.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final ErrorLogService errorLogService;

    public GlobalExceptionHandler(ErrorLogService errorLogService) {
        this.errorLogService = errorLogService;
    }

    @ExceptionHandler(ResponseStatusException.class)
    public Object handleStatusException(ResponseStatusException ex, HttpServletRequest request,
            Model model, RedirectAttributes redirectAttributes) {

        errorLogService.recordException(
                "Request rejected with " + ex.getStatusCode().value() + " - " + ex.getReason(),
                ex,
                "Request stopped by NoteController ownership/status checks; client is asked to correct the request.");

        if (isHtmx(request)) {
            model.addAttribute("errorMessage", ex.getReason());
            return "fragments/error-alert";
        }
        if (ex.getStatusCode().is4xxClientError()) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getReason());
            return "redirect:/";
        }
        model.addAttribute("errorTitle", ex.getStatusCode().value() + " - " + ex.getReason());
        model.addAttribute("statusCode", ex.getStatusCode().value());
        return "pages/error";
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Object handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request,
            Model model) {

        errorLogService.recordException("Bad request input", ex,
                "Input rejected before it reached the service layer.");

        if (isHtmx(request)) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "fragments/error-alert";
        }
        return renderErrorPage(model, 400, ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Object handleIllegalState(IllegalStateException ex, HttpServletRequest request, Model model) {
        errorLogService.recordException("Conflicting state", ex,
                "Operation refused because existing data would be violated.");

        if (isHtmx(request)) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "fragments/error-alert";
        }
        return renderErrorPage(model, 409, ex.getMessage());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Object handleNotFound(NoResourceFoundException ex, HttpServletRequest request, Model model) {
        // A missing page is a client mistake, not a fault worth a row in ErrorEnquiry, so this
        // handler only renders the page.
        if (isHtmx(request)) {
            model.addAttribute("errorMessage", "That content was not found.");
            return "fragments/error-alert";
        }
        return renderErrorPage(model, 404, "That page does not exist.");
    }

    @ExceptionHandler(Exception.class)
    public Object handleUnexpected(Exception ex, HttpServletRequest request, Model model) {
        log.error("Unhandled exception on {} {}", request.getMethod(), request.getRequestURI(), ex);
        errorLogService.recordException("Unhandled runtime error", ex,
                "Captured by GlobalExceptionHandler; the stack trace is in the ErrorEnquiry table.");

        if (isHtmx(request)) {
            model.addAttribute("errorMessage", "Something went wrong. Please try again.");
            return "fragments/error-alert";
        }
        return renderErrorPage(model, 500, "Something went wrong on our side. The error was recorded.");
    }

    private String renderErrorPage(Model model, int status, String message) {
        model.addAttribute("errorTitle", status + " - " + message);
        model.addAttribute("statusCode", status);
        model.addAttribute("errorMessage", message);
        return "pages/error";
    }

    private boolean isHtmx(HttpServletRequest request) {
        return "true".equals(request.getHeader("HX-Request"));
    }
}
