package com.embervault.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import com.embervault.adapter.out.persistence.InMemoryNoteRepository;
import com.embervault.adapter.out.persistence.InMemoryStampRepository;
import com.embervault.application.port.in.StampService;
import com.embervault.domain.AttributeValue;
import com.embervault.domain.Note;
import com.embervault.domain.Stamp;
import com.embervault.domain.TbxColor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StampServiceImplTest {

    private StampService stampService;
    private InMemoryStampRepository stampRepository;
    private InMemoryNoteRepository noteRepository;

    @BeforeEach
    void setUp() {
        stampRepository = new InMemoryStampRepository();
        noteRepository = new InMemoryNoteRepository();
        stampService = new StampServiceImpl(stampRepository, noteRepository);
    }

    @Test
    @DisplayName("createStamp() persists and returns a stamp")
    void createStamp_shouldPersistAndReturn() {
        Stamp stamp = stampService.createStamp("Color:red", "$Color=red");

        assertNotNull(stamp);
        assertNotNull(stamp.id());
        assertEquals("Color:red", stamp.name());
        assertEquals("$Color=red", stamp.action());
        assertTrue(stampRepository.findById(stamp.id()).isPresent());
    }

    @Test
    @DisplayName("getAllStamps() returns all created stamps")
    void getAllStamps_shouldReturnAll() {
        stampService.createStamp("A", "$A=1");
        stampService.createStamp("B", "$B=2");

        List<Stamp> stamps = stampService.getAllStamps();

        assertEquals(2, stamps.size());
    }

    @Test
    @DisplayName("getStamp() returns stamp by id")
    void getStamp_shouldReturnById() {
        Stamp created = stampService.createStamp("Color:red", "$Color=red");

        Optional<Stamp> found = stampService.getStamp(created.id());

        assertTrue(found.isPresent());
        assertEquals(created, found.get());
    }

    @Test
    @DisplayName("getStamp() returns empty for unknown id")
    void getStamp_shouldReturnEmptyForUnknown() {
        Optional<Stamp> found = stampService.getStamp(UUID.randomUUID());

        assertTrue(found.isEmpty());
    }

    @Test
    @DisplayName("deleteStamp() removes the stamp")
    void deleteStamp_shouldRemoveStamp() {
        Stamp stamp = stampService.createStamp("Color:red", "$Color=red");

        stampService.deleteStamp(stamp.id());

        assertTrue(stampService.getStamp(stamp.id()).isEmpty());
    }

    @Test
    @DisplayName("applyStamp() sets color attribute on note")
    void applyStamp_shouldSetColorOnNote() {
        Note note = Note.create("Test", "content");
        noteRepository.save(note);
        Stamp stamp = stampService.createStamp("Color:red", "$Color=red");

        stampService.applyStamp(stamp.id(), note.getId());

        Note updated = noteRepository.findById(note.getId()).orElseThrow();
        AttributeValue value = updated.getAttribute("$Color").orElseThrow();
        assertNotNull(value);
        assertEquals(TbxColor.named("red"),
                ((AttributeValue.ColorValue) value).value());
    }

    @Test
    @DisplayName("applyStamp() sets boolean attribute on note")
    void applyStamp_shouldSetBooleanOnNote() {
        Note note = Note.create("Test", "content");
        noteRepository.save(note);
        Stamp stamp = stampService.createStamp("Mark Done", "$Checked=true");

        stampService.applyStamp(stamp.id(), note.getId());

        Note updated = noteRepository.findById(note.getId()).orElseThrow();
        AttributeValue value = updated.getAttribute("$Checked").orElseThrow();
        assertEquals(true, ((AttributeValue.BooleanValue) value).value());
    }

    @Test
    @DisplayName("applyStamp() sets number attribute on note")
    void applyStamp_shouldSetNumberOnNote() {
        Note note = Note.create("Test", "content");
        noteRepository.save(note);
        Stamp stamp = stampService.createStamp("Priority", "$Priority=5");

        stampService.applyStamp(stamp.id(), note.getId());

        Note updated = noteRepository.findById(note.getId()).orElseThrow();
        AttributeValue value = updated.getAttribute("$Priority").orElseThrow();
        assertEquals(5.0, ((AttributeValue.NumberValue) value).value());
    }

    @Test
    @DisplayName("applyStamp() sets string attribute on note")
    void applyStamp_shouldSetStringOnNote() {
        Note note = Note.create("Test", "content");
        noteRepository.save(note);
        Stamp stamp = stampService.createStamp("Priority:high", "$Priority=high");

        stampService.applyStamp(stamp.id(), note.getId());

        Note updated = noteRepository.findById(note.getId()).orElseThrow();
        AttributeValue value = updated.getAttribute("$Priority").orElseThrow();
        assertEquals("high", ((AttributeValue.StringValue) value).value());
    }

    @Test
    @DisplayName("applyStamp() throws when stamp not found")
    void applyStamp_shouldThrowForMissingStamp() {
        Note note = Note.create("Test", "content");
        noteRepository.save(note);

        assertThrows(NoSuchElementException.class,
                () -> stampService.applyStamp(UUID.randomUUID(), note.getId()));
    }

    @Test
    @DisplayName("applyStamp() throws when note not found")
    void applyStamp_shouldThrowForMissingNote() {
        Stamp stamp = stampService.createStamp("Color:red", "$Color=red");

        assertThrows(NoSuchElementException.class,
                () -> stampService.applyStamp(stamp.id(), UUID.randomUUID()));
    }

    @Test
    @DisplayName("constructor rejects null stamp repository")
    void constructor_shouldRejectNullStampRepository() {
        assertThrows(NullPointerException.class,
                () -> new StampServiceImpl(null, noteRepository));
    }

    @Test
    @DisplayName("constructor rejects null note repository")
    void constructor_shouldRejectNullNoteRepository() {
        assertThrows(NullPointerException.class,
                () -> new StampServiceImpl(stampRepository, null));
    }
}
