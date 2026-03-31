package com.embervault.application;

import java.util.ArrayDeque;
import java.util.Deque;

import com.embervault.application.port.out.NoteRepository;
import com.embervault.domain.Note;
import com.embervault.domain.NoteMemento;

/**
 * Manages undo/redo stacks using the Memento pattern.
 *
 * <p>Before each mutation, callers record the note's current state via
 * {@link #recordChange(Note)}. Undo restores the most recent memento
 * and pushes the current state onto the redo stack.</p>
 */
public class UndoRedoService {

    private static final int MAX_STACK_SIZE = 50;

    private final Deque<NoteMemento> undoStack = new ArrayDeque<>();
    private final Deque<NoteMemento> redoStack = new ArrayDeque<>();

    /**
     * Records the current state of a note before it is mutated.
     *
     * @param note the note about to be changed
     */
    public void recordChange(Note note) {
        undoStack.push(NoteMemento.capture(note));
        redoStack.clear();
        if (undoStack.size() > MAX_STACK_SIZE) {
            undoStack.removeLast();
        }
    }

    /**
     * Undoes the most recent change by restoring the note from the
     * top memento and pushing the current state onto the redo stack.
     *
     * @param repository the repository to retrieve and save the note
     */
    public void undo(NoteRepository repository) {
        if (undoStack.isEmpty()) {
            return;
        }
        NoteMemento memento = undoStack.pop();
        Note note = repository.findById(memento.getNoteId()).orElseThrow();
        redoStack.push(NoteMemento.capture(note));
        memento.restore(note);
        repository.save(note);
    }

    /**
     * Redoes the most recently undone change.
     *
     * @param repository the repository to retrieve and save the note
     */
    public void redo(NoteRepository repository) {
        if (redoStack.isEmpty()) {
            return;
        }
        NoteMemento memento = redoStack.pop();
        Note note = repository.findById(memento.getNoteId()).orElseThrow();
        undoStack.push(NoteMemento.capture(note));
        memento.restore(note);
        repository.save(note);
    }

    /** Returns whether there are changes that can be undone. */
    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    /** Returns whether there are changes that can be redone. */
    public boolean canRedo() {
        return !redoStack.isEmpty();
    }
}
