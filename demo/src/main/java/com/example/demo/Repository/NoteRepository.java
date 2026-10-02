package com.example.demo.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.Model.Note;

public interface NoteRepository extends JpaRepository<Note, Long> {

    /** Owner is fetched eagerly: the grid renders the author name without a query per card. */
    @EntityGraph(attributePaths = "owner")
    List<Note> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = "owner")
    Optional<Note> findWithOwnerById(Long id);
}
