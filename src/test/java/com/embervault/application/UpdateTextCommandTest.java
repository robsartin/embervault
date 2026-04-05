package com.embervault.application;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.embervault.adapter.out.persistence.InMemoryNoteRepository;
import com.embervault.application.port.in.UpdateNoteTextUseCase;
import com.embervault.domain.Note;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UpdateTextCommandTest {

    private UpdateNoteTextUseCase updateTextUseCase;
    private InMemoryNoteRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryNoteRepository();
        NoteServiceImpl service = new NoteServiceImpl(repository);
        updateTextUseCase = service;
    }

    @Test
    @DisplayName("undo restores previous text")
    void undo_restoresPreviousText() {
        Note note = Note.create("Title", "old text");
        repository.save(note);

        UpdateTextCommand cmd = new UpdateTextCommand(
                updateTextUseCase, note.getId(), "old text", "new text");
        cmd.redo();

        assertEquals("new text", repository.findById(note.getId())
                .orElseThrow().getText());

        cmd.undo();

        assertEquals("old text", repository.findById(note.getId())
                .orElseThrow().getText());
    }

    @Test
    @DisplayName("redo re-applies the text change")
    void redo_reappliesTextChange() {
        Note note = Note.create("Title", "old text");
        repository.save(note);

        UpdateTextCommand cmd = new UpdateTextCommand(
                updateTextUseCase, note.getId(), "old text", "new text");
        cmd.redo();
        cmd.undo();
        cmd.redo();

        assertEquals("new text", repository.findById(note.getId())
                .orElseThrow().getText());
    }

    @Test
    @DisplayName("description mentions text update")
    void description_mentionsTextUpdate() {
        UpdateTextCommand cmd = new UpdateTextCommand(
                updateTextUseCase, java.util.UUID.randomUUID(),
                "old", "new");

        assertEquals("Update note text", cmd.description());
    }
}
