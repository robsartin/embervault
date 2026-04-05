package com.embervault.application;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.embervault.adapter.out.persistence.InMemoryNoteRepository;
import com.embervault.application.port.in.CreateNoteUseCase;
import com.embervault.application.port.in.RenameNoteUseCase;
import com.embervault.domain.Note;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RenameCommandTest {

    private RenameNoteUseCase renameUseCase;
    private CreateNoteUseCase creator;
    private InMemoryNoteRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryNoteRepository();
        NoteServiceImpl service = new NoteServiceImpl(repository);
        renameUseCase = service;
        creator = service;
    }

    @Test
    @DisplayName("undo restores the previous title")
    void undo_restoresPreviousTitle() {
        Note note = creator.createNote("Original", "content");
        RenameCommand cmd = new RenameCommand(
                renameUseCase, note.getId(), "Original", "Renamed");
        cmd.redo();

        assertEquals("Renamed", repository.findById(note.getId())
                .orElseThrow().getTitle());

        cmd.undo();

        assertEquals("Original", repository.findById(note.getId())
                .orElseThrow().getTitle());
    }

    @Test
    @DisplayName("redo re-applies the rename")
    void redo_reappliesRename() {
        Note note = creator.createNote("Original", "content");
        RenameCommand cmd = new RenameCommand(
                renameUseCase, note.getId(), "Original", "Renamed");
        cmd.redo();
        cmd.undo();
        cmd.redo();

        assertEquals("Renamed", repository.findById(note.getId())
                .orElseThrow().getTitle());
    }

    @Test
    @DisplayName("description mentions rename")
    void description_mentionsRename() {
        Note note = creator.createNote("Old", "content");
        RenameCommand cmd = new RenameCommand(
                renameUseCase, note.getId(), "Old", "New");

        assertEquals("Rename 'Old' to 'New'", cmd.description());
    }
}
