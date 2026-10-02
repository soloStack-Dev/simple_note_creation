package com.example.demo.Service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.example.demo.Model.AuthSource;
import com.example.demo.Model.Category;
import com.example.demo.Model.Note;
import com.example.demo.Repository.NoteRepository;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class NoteServiceTest {

    private static final Instant NOW = Instant.parse("2026-01-02T03:04:05Z");

    @Mock
    private NoteRepository repository;

    private NoteService noteService;
    private AuthSource owner;

    @BeforeEach
    void setUp() {
        noteService = new NoteService(repository, Clock.fixed(NOW, ZoneOffset.UTC));
        owner = new AuthSource("someone", "a@b.com", "hash", null);
        when(repository.save(any(Note.class))).thenAnswer(call -> call.getArgument(0));
    }

    @Test
    void validateRejectsABlankMessage() {
        assertThat(noteService.validate("   ")).containsExactly("Message is required.");
        assertThat(noteService.validate(null)).containsExactly("Message is required.");
    }

    @Test
    void validateAcceptsAMessageAtTheLengthLimit() {
        String message = "x".repeat(NoteService.MAX_MESSAGE_LENGTH);
        assertThat(noteService.validate(message)).isEmpty();
    }

    @Test
    void validateRejectsAMessageOverTheLimit() {
        String message = "x".repeat(NoteService.MAX_MESSAGE_LENGTH + 1);
        assertThat(noteService.validate(message))
                .containsExactly("Message must be " + NoteService.MAX_MESSAGE_LENGTH + " characters or fewer.");
    }

    @Test
    void createTrimsTheMessageAndStampsBothTimestamps() {
        when(repository.save(any(Note.class))).thenAnswer(call -> call.getArgument(0));

        Note note = noteService.create(Category.MEMORIES, "  a good day  ", owner);

        assertThat(note.getMessage()).isEqualTo("a good day");
        assertThat(note.getCategory()).isEqualTo(Category.MEMORIES);
        assertThat(note.getOwner()).isSameAs(owner);
        assertThat(note.getCreatedAt()).isEqualTo(NOW);
        assertThat(note.getUpdatedAt()).isEqualTo(NOW);
    }

    @Test
    void createFallsBackToSomethingElseWhenTheCategoryIsMissing() {
        when(repository.save(any(Note.class))).thenAnswer(call -> call.getArgument(0));

        assertThat(noteService.create(null, "text", owner).getCategory())
                .isEqualTo(Category.SOMETHING_ELSE);
    }

    @Test
    void updateKeepsTheCategoryWhenNoneIsGiven() {
        Note note = noteService.create(Category.MEMORIES, "before", owner);

        noteService.update(note, null, "after");

        assertThat(note.getCategory()).isEqualTo(Category.MEMORIES);
        assertThat(note.getMessage()).isEqualTo("after");
        assertThat(note.getUpdatedAt()).isEqualTo(NOW);
    }

    @Test
    void deleteRemovesById() {
        noteService.delete(7L);

        verify(repository).deleteById(7L);
    }

    @Test
    void findAllReturnsNewestFirst() {
        Note note = noteService.create(Category.MEMORIES, "text", owner);
        when(repository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(note));

        assertThat(noteService.findAll()).containsExactly(note);
    }

    @Test
    void findByIdIsEmptyForAnUnknownNote() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThat(noteService.findById(99L)).isEmpty();
    }
}
