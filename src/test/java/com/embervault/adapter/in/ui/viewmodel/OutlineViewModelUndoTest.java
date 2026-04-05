package com.embervault.adapter.in.ui.viewmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.embervault.adapter.out.persistence.InMemoryNoteRepository;
import com.embervault.application.CommandHistory;
import com.embervault.application.NoteServiceImpl;
import com.embervault.application.UndoRedoService;
import com.embervault.application.port.in.NoteService;
import com.embervault.domain.Note;
import java.util.UUID;
import javafx.beans.property.SimpleStringProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OutlineViewModelUndoTest {

    private OutlineViewModel viewModel;
    private NoteService noteService;
    private InMemoryNoteRepository repository;
    private UndoRedoService undoRedoService;

    @BeforeEach
    void setUp() {
        repository = new InMemoryNoteRepository();
        noteService = new NoteServiceImpl(repository);
        EventBus eventBus = new EventBus();
        AppState appState = new AppState();
        CommandHistory history = new CommandHistory();
        undoRedoService = new UndoRedoService(history);
        viewModel = new OutlineViewModel(
                new SimpleStringProperty("Root"),
                noteService, noteService, noteService,
                noteService, noteService, noteService,
                appState, eventBus);
        viewModel.setCommandRecorder(undoRedoService);
    }

    @Test
    @DisplayName("rename records an undoable command")
    void rename_recordsUndoableCommand() {
        Note parent = Note.create("Parent", "");
        repository.save(parent);
        Note child = noteService.createChildNote(
                parent.getId(), "Original");
        viewModel.setBaseNoteId(parent.getId());
        viewModel.loadNotes();

        viewModel.renameNote(child.getId(), "Renamed");

        assertTrue(undoRedoService.canUndo());
    }

    @Test
    @DisplayName("undo rename restores the original title")
    void undoRename_restoresOriginalTitle() {
        Note parent = Note.create("Parent", "");
        repository.save(parent);
        Note child = noteService.createChildNote(
                parent.getId(), "Original");
        viewModel.setBaseNoteId(parent.getId());
        viewModel.loadNotes();

        viewModel.renameNote(child.getId(), "Renamed");
        undoRedoService.undo();

        assertEquals("Original",
                repository.findById(child.getId())
                        .orElseThrow().getTitle());
    }

    @Test
    @DisplayName("redo rename re-applies the new title")
    void redoRename_reappliesNewTitle() {
        Note parent = Note.create("Parent", "");
        repository.save(parent);
        Note child = noteService.createChildNote(
                parent.getId(), "Original");
        viewModel.setBaseNoteId(parent.getId());
        viewModel.loadNotes();

        viewModel.renameNote(child.getId(), "Renamed");
        undoRedoService.undo();
        undoRedoService.redo();

        assertEquals("Renamed",
                repository.findById(child.getId())
                        .orElseThrow().getTitle());
    }

    @Test
    @DisplayName("create child note records an undoable command")
    void createChild_recordsUndoableCommand() {
        Note parent = Note.create("Parent", "");
        repository.save(parent);
        viewModel.setBaseNoteId(parent.getId());
        viewModel.loadNotes();

        viewModel.createChildNote(parent.getId(), "New Child");

        assertTrue(undoRedoService.canUndo());
    }

    @Test
    @DisplayName("undo create deletes the created note")
    void undoCreate_deletesCreatedNote() {
        Note parent = Note.create("Parent", "");
        repository.save(parent);
        viewModel.setBaseNoteId(parent.getId());
        viewModel.loadNotes();

        NoteDisplayItem item = viewModel.createChildNote(
                parent.getId(), "New Child");
        UUID childId = item.getId();
        assertTrue(repository.findById(childId).isPresent());

        undoRedoService.undo();

        assertFalse(repository.findById(childId).isPresent());
    }

    @Test
    @DisplayName("rename without recorder does not throw")
    void rename_withoutRecorder_doesNotThrow() {
        viewModel.setCommandRecorder(null);
        Note parent = Note.create("Parent", "");
        repository.save(parent);
        Note child = noteService.createChildNote(
                parent.getId(), "Original");
        viewModel.setBaseNoteId(parent.getId());
        viewModel.loadNotes();

        assertTrue(viewModel.renameNote(child.getId(), "Renamed"));
        assertFalse(undoRedoService.canUndo());
    }
}
