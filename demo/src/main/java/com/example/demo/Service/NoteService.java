package com.example.demo.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.Model.Category;
import com.example.demo.Model.Note;
import com.example.demo.Repository.NoteRepository;

@Service
public class NoteService {

    /** Longest note body we accept. The UI uses a large textarea, but the limit is enforced server side. */
    public static final int MAX_MESSAGE_LENGTH = 5000;

    private final NoteRepository repository;
    private final Clock clock;

    public NoteService(NoteRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<Note> findAll() {
        return repository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public Optional<Note> findById(Long id) {
        return repository.findById(id);
    }

    /**
     * How many notes sit in each category, keyed by {@link Category#name()}, with every category
     * present even when it is empty.
     *
     * <p>Backs the stat strip above the notes grid. Keyed by the enum's name rather than by the
     * enum itself because that is the only thing the template can hold: SpEL cannot index an
     * {@code EnumMap} with a string key. A missing key would render a blank cell rather than a
     * zero, so every category is seeded.
     */
    @Transactional(readOnly = true)
    public Map<String, Long> countByCategory() {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (Category category : Category.values()) {
            counts.put(category.name(), 0L);
        }
        for (Note note : findAll()) {
            counts.merge(note.getCategory().name(), 1L, Long::sum);
        }
        return counts;
    }

    @Transactional(readOnly = true)
    public List<String> validate(String message) {
        List<String> errors = new java.util.ArrayList<>();
        if (isBlank(message)) {
            errors.add("Message is required.");
        } else if (message.trim().length() > MAX_MESSAGE_LENGTH) {
            errors.add("Message must be " + MAX_MESSAGE_LENGTH + " characters or fewer.");
        }
        return errors;
    }

    @Transactional
    public Note create(Category category, String message, com.example.demo.Model.AuthSource owner) {
        Note note = new Note(category == null ? Category.SOMETHING_ELSE : category,
                message.trim(), Instant.now(clock), owner);
        note.setUpdatedAt(note.getCreatedAt());
        return repository.save(note);
    }

    @Transactional
    public Note update(Note note, Category category, String message) {
        if (category != null) {
            note.setCategory(category);
        }
        note.setMessage(message.trim());
        note.setUpdatedAt(Instant.now(clock));
        return repository.save(note);
    }

    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
