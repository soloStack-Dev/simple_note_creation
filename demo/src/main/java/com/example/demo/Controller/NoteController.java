package com.example.demo.Controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

import com.example.demo.Model.AuthSource;
import com.example.demo.Model.Category;
import com.example.demo.Model.Note;
import com.example.demo.Service.AuthService;
import com.example.demo.Service.ErrorLogService;
import com.example.demo.Service.NoteService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * The Note page plus the HTMX fragment endpoints that back it.
 *
 * <p>Fragment routes return partials with no {@code <html>} wrapper so htmx can swap them into
 * the grid. Mutations answer with a real status code - 422 when the form does not validate
 * (htmx is configured to swap 422 bodies in app.js), 404 for a missing note, 403 when the
 * caller does not own it.
 */
@Controller
public class NoteController {

    private final NoteService noteService;
    private final AuthService authService;
    private final ErrorLogService errorLogService;

    public NoteController(NoteService noteService, AuthService authService, ErrorLogService errorLogService) {
        this.noteService = noteService;
        this.authService = authService;
        this.errorLogService = errorLogService;
    }

    @GetMapping("/notes")
    public String notesPage(Model model, HttpServletResponse response) {
        model.addAttribute("pageTitle", "Note");
        model.addAllAttributes(gridAttributes());
        // The add-note form is rendered into the modal server side, so the page needs
        // the same model the fragment route would provide.
        model.addAttribute("categories", List.of(Category.values()));
        model.addAttribute("errors", List.of());
        model.addAttribute("selectedCategory", Category.MEMORIES);
        markVaryOnHtmx(response);
        return "pages/notes";
    }

    /** Whole grid: first paint, and the response to a create or delete. */
    @GetMapping("/fragments/notes-grid")
    public String notesGrid(Model model) {
        model.addAllAttributes(gridAttributes());
        return "fragments/notes-grid";
    }

    /**
     * Everything the grid fragment needs, in one place.
     *
     * <p>The fragment is rendered from four different routes - first paint, the cancel button on
     * an inline edit, and the out-of-band part of a create or a delete - so the model has to be
     * built the same way each time or a later swap silently renders a half-populated strip.
     * Returned as a plain map because {@link Model} and {@link ModelAndView} do not share a
     * supertype for attributes in this Spring version, but both accept a map.
     */
    private Map<String, Object> gridAttributes() {
        List<Note> notes = noteService.findAll();
        Map<String, Object> attributes = new LinkedHashMap<>();
        attributes.put("notes", notes);
        attributes.put("noteCounts", noteService.countByCategory());
        attributes.put("noteTotal", notes.size());
        return attributes;
    }

    /**
 * Opts the response into {@code Vary: HX-Request}.
 *
 * <p>These routes answer with a full page to a browser and an HTML fragment to htmx, off the same
 * URL. That is only safe if anything holding a copy of the response keys on the header, otherwise
 * a cached fragment gets served to a browser that asked for a page. Set here rather than from an
 * advice: {@code @ModelAttribute} methods run <em>before</em> the handler, so an advice would
 * read the opt-in before the handler had set it.
 *
 * <p>Appended rather than assigned, because Spring Security writes its own {@code Vary} for CORS
 * on responses that need it.
 */
private void markVaryOnHtmx(HttpServletResponse response) {
    String existing = response.getHeader("Vary");
    if (existing == null || existing.isBlank()) {
        response.setHeader("Vary", "HX-Request");
    } else if (!existing.contains("HX-Request")) {
        response.setHeader("Vary", existing + ", HX-Request");
    }
}

    /** Empty form for the "Add New Note" modal. */
    @GetMapping("/fragments/note-form")
    public String newNoteForm(Model model) {
        model.addAttribute("categories", List.of(Category.values()));
        model.addAttribute("errors", List.of());
        model.addAttribute("selectedCategory", Category.MEMORIES);
        return "fragments/note-form";
    }

    @PostMapping("/notes")
    public ModelAndView createNote(@RequestParam String category,
            @RequestParam String message,
            HttpServletRequest request, HttpServletResponse response) {

        AuthSource owner = requireAuthenticatedUser();
        markVaryOnHtmx(response);
        List<String> errors = noteService.validate(message);
        if (!errors.isEmpty()) {
            if (!isHtmx(request)) {
                return new ModelAndView("redirect:/notes");
            }
            ModelAndView mav = new ModelAndView("fragments/note-form");
            mav.setStatus(HttpStatus.UNPROCESSABLE_ENTITY);
            mav.addObject("categories", List.of(Category.values()));
            mav.addObject("errors", errors);
            mav.addObject("selectedCategory", Category.from(category));
            mav.addObject("message", message);
            return mav;
        }

        noteService.create(Category.from(category), message, owner);
        if (!isHtmx(request)) {
            return new ModelAndView("redirect:/notes");
        }
        // Grid goes out-of-band; the modal is closed by app.js on the marker.
        ModelAndView mav = new ModelAndView("fragments/note-created");
        mav.addAllObjects(gridAttributes());
        return mav;
    }

    @GetMapping("/fragments/note/{id}/edit")
    public String editNoteForm(@PathVariable Long id, Model model, HttpServletResponse response) {
        markVaryOnHtmx(response);
        Note note = requireOwnedNote(id);
        model.addAttribute("note", note);
        model.addAttribute("categories", List.of(Category.values()));
        model.addAttribute("errors", List.of());
        model.addAttribute("selectedCategory", note.getCategory());
        model.addAttribute("message", note.getMessage());
        return "fragments/note-edit-form";
    }

    @PostMapping("/notes/{id}")
    public ModelAndView updateNote(@PathVariable Long id,
            @RequestParam String category,
            @RequestParam String message,
            HttpServletRequest request, HttpServletResponse response) {

        Note note = requireOwnedNote(id);
        List<String> errors = noteService.validate(message);
        if (!errors.isEmpty()) {
            if (!isHtmx(request)) {
                return new ModelAndView("redirect:/notes");
            }
            ModelAndView mav = new ModelAndView("fragments/note-edit-form");
            mav.setStatus(HttpStatus.UNPROCESSABLE_ENTITY);
            mav.addObject("note", note);
            mav.addObject("categories", List.of(Category.values()));
            mav.addObject("errors", errors);
            mav.addObject("selectedCategory", Category.from(category));
            mav.addObject("message", message);
            return mav;
        }

        noteService.update(note, Category.from(category), message);
        if (!isHtmx(request)) {
            return new ModelAndView("redirect:/notes");
        }
        return new ModelAndView("fragments/note-card", "note", noteService.findById(note.getId()).orElse(note));
    }

    @DeleteMapping("/notes/{id}")
    public ModelAndView deleteNote(@PathVariable Long id, HttpServletRequest request,
            HttpServletResponse response) {
        Note note = requireOwnedNote(id);
        markVaryOnHtmx(response);
        noteService.delete(note.getId());
        if (!isHtmx(request)) {
            return new ModelAndView("redirect:/notes");
        }
        // Re-render the grid so the "no notes are available" state returns with the last note.
        ModelAndView mav = new ModelAndView("fragments/notes-grid");
        mav.addAllObjects(gridAttributes());
        return mav;
    }

    /** htmx sends HX-Request; a plain browser form does not. */
    private boolean isHtmx(HttpServletRequest request) {
        return "true".equals(request.getHeader("HX-Request"));
    }

    private AuthSource requireAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sign in to manage notes.");
        }
        return authService.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "Account no longer exists."));
    }

    private Note requireOwnedNote(Long id) {
        Note note = noteService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found."));
        AuthSource owner = requireAuthenticatedUser();
        if (!note.getOwner().getId().equals(owner.getId())) {
            errorLogService.record(
                    "Blocked note edit",
                    "User '" + owner.getUsername() + "' tried to change note " + id + " owned by '"
                            + note.getOwner().getUsername() + "'",
                    "Ownership check in NoteController.requireOwnedNote.",
                    "Request rejected with 403; no data was changed.");
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only change your own notes.");
        }
        return note;
    }
}
