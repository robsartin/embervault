package com.embervault.adapter.in.ui.viewmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import com.embervault.adapter.out.persistence.InMemoryNoteRepository;
import com.embervault.application.NoteServiceImpl;
import com.embervault.application.port.in.NoteService;
import com.embervault.domain.AttributeSchemaRegistry;
import com.embervault.domain.AttributeType;
import com.embervault.domain.AttributeValue;
import com.embervault.domain.Note;
import com.embervault.domain.TbxColor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NoteEditorViewModelTest {

    private NoteEditorViewModel viewModel;
    private NoteService noteService;
    private InMemoryNoteRepository repository;
    private AttributeSchemaRegistry schemaRegistry;

    @BeforeEach
    void setUp() {
        repository = new InMemoryNoteRepository();
        noteService = new NoteServiceImpl(repository);
        schemaRegistry = new AttributeSchemaRegistry();
        viewModel = new NoteEditorViewModel(noteService, schemaRegistry);
    }

    @Test
    @DisplayName("Constructor rejects null noteService")
    void constructor_shouldRejectNullNoteService() {
        assertThrows(NullPointerException.class,
                () -> new NoteEditorViewModel(null, schemaRegistry));
    }

    @Test
    @DisplayName("Constructor rejects null schemaRegistry")
    void constructor_shouldRejectNullSchemaRegistry() {
        assertThrows(NullPointerException.class,
                () -> new NoteEditorViewModel(noteService, null));
    }

    @Test
    @DisplayName("Initial state has empty title and text")
    void initialState_shouldHaveEmptyTitleAndText() {
        assertEquals("", viewModel.titleProperty().get());
        assertEquals("", viewModel.textProperty().get());
        assertTrue(viewModel.getEditableAttributes().isEmpty());
        assertNull(viewModel.getCurrentNoteId());
    }

    @Test
    @DisplayName("setNote loads note title and text")
    void setNote_shouldLoadTitleAndText() {
        Note note = noteService.createNote("Test Title", "Test Content");

        viewModel.setNote(note.getId());

        assertEquals("Test Title", viewModel.titleProperty().get());
        assertEquals("Test Content", viewModel.textProperty().get());
        assertEquals(note.getId(), viewModel.getCurrentNoteId());
    }

    @Test
    @DisplayName("setNote(null) clears the editor")
    void setNote_null_shouldClearEditor() {
        Note note = noteService.createNote("Test", "Content");
        viewModel.setNote(note.getId());

        viewModel.setNote(null);

        assertEquals("", viewModel.titleProperty().get());
        assertEquals("", viewModel.textProperty().get());
        assertTrue(viewModel.getEditableAttributes().isEmpty());
        assertNull(viewModel.getCurrentNoteId());
    }

    @Test
    @DisplayName("setNote loads editable attributes excluding $Name and $Text")
    void setNote_shouldLoadAttributesExcludingNameAndText() {
        Note note = noteService.createNote("Test", "Content");
        note.setAttribute("$Prototype",
                new AttributeValue.StringValue("MyProto"));

        viewModel.setNote(note.getId());

        // Should not contain $Name or $Text
        boolean hasName = viewModel.getEditableAttributes().stream()
                .anyMatch(a -> "$Name".equals(a.getName()));
        boolean hasText = viewModel.getEditableAttributes().stream()
                .anyMatch(a -> "$Text".equals(a.getName()));
        assertFalse(hasName, "$Name should not appear in editable attributes");
        assertFalse(hasText, "$Text should not appear in editable attributes");

        // Should contain $Prototype
        AttributeEntry protoEntry = viewModel.getEditableAttributes().stream()
                .filter(a -> "$Prototype".equals(a.getName()))
                .findFirst().orElse(null);
        assertNotNull(protoEntry);
        assertEquals("MyProto", protoEntry.getValue());
        assertEquals(AttributeType.STRING, protoEntry.getType());
    }

    @Test
    @DisplayName("setNote excludes read-only attributes")
    void setNote_shouldExcludeReadOnlyAttributes() {
        Note note = noteService.createNote("Test", "Content");

        viewModel.setNote(note.getId());

        // $Created and $Modified are read-only and should not appear
        boolean hasCreated = viewModel.getEditableAttributes().stream()
                .anyMatch(a -> "$Created".equals(a.getName()));
        boolean hasModified = viewModel.getEditableAttributes().stream()
                .anyMatch(a -> "$Modified".equals(a.getName()));
        assertFalse(hasCreated, "$Created is read-only");
        assertFalse(hasModified, "$Modified is read-only");
    }

    @Test
    @DisplayName("saveTitle persists and updates title property")
    void saveTitle_shouldPersistAndUpdateProperty() {
        Note note = noteService.createNote("Old Title", "Content");
        viewModel.setNote(note.getId());

        viewModel.saveTitle("New Title");

        assertEquals("New Title", viewModel.titleProperty().get());
        // Verify persistence
        Note reloaded = noteService.getNote(note.getId()).orElseThrow();
        assertEquals("New Title", reloaded.getTitle());
    }

    @Test
    @DisplayName("saveTitle does nothing when no note is loaded")
    void saveTitle_shouldDoNothingWhenNoNote() {
        viewModel.saveTitle("Something");
        // Should not throw
        assertEquals("", viewModel.titleProperty().get());
    }

    @Test
    @DisplayName("saveTitle does nothing for blank title")
    void saveTitle_shouldDoNothingForBlankTitle() {
        Note note = noteService.createNote("Original", "Content");
        viewModel.setNote(note.getId());

        viewModel.saveTitle("   ");

        assertEquals("Original", viewModel.titleProperty().get());
    }

    @Test
    @DisplayName("saveText persists and updates text property")
    void saveText_shouldPersistAndUpdateProperty() {
        Note note = noteService.createNote("Title", "Old Content");
        viewModel.setNote(note.getId());

        viewModel.saveText("New Content");

        assertEquals("New Content", viewModel.textProperty().get());
        // Verify persistence
        Note reloaded = noteService.getNote(note.getId()).orElseThrow();
        assertEquals("New Content", reloaded.getContent());
    }

    @Test
    @DisplayName("saveText does nothing when no note is loaded")
    void saveText_shouldDoNothingWhenNoNote() {
        viewModel.saveText("Something");
        assertEquals("", viewModel.textProperty().get());
    }

    @Test
    @DisplayName("saveAttribute persists a string attribute value")
    void saveAttribute_shouldPersistStringValue() {
        Note note = noteService.createNote("Title", "Content");
        viewModel.setNote(note.getId());

        viewModel.saveAttribute("$Prototype", "NewProto");

        Note reloaded = noteService.getNote(note.getId()).orElseThrow();
        AttributeValue.StringValue val = (AttributeValue.StringValue)
                reloaded.getAttribute("$Prototype").orElseThrow();
        assertEquals("NewProto", val.value());
    }

    @Test
    @DisplayName("saveAttribute updates editable attributes list")
    void saveAttribute_shouldUpdateEditableAttributesList() {
        Note note = noteService.createNote("Title", "Content");
        note.setAttribute("$Prototype",
                new AttributeValue.StringValue("OldValue"));
        viewModel.setNote(note.getId());

        viewModel.saveAttribute("$Prototype", "NewValue");

        AttributeEntry entry = viewModel.getEditableAttributes().stream()
                .filter(a -> "$Prototype".equals(a.getName()))
                .findFirst().orElse(null);
        assertNotNull(entry);
        assertEquals("NewValue", entry.getValue());
    }

    @Test
    @DisplayName("saveAttribute does nothing when no note is loaded")
    void saveAttribute_shouldDoNothingWhenNoNote() {
        viewModel.saveAttribute("$Prototype", "Something");
        // Should not throw
        assertTrue(viewModel.getEditableAttributes().isEmpty());
    }

    @Test
    @DisplayName("saveTitle notifies data changed")
    void saveTitle_shouldNotifyDataChanged() {
        Note note = noteService.createNote("Title", "Content");
        viewModel.setNote(note.getId());
        boolean[] notified = {false};
        viewModel.setOnDataChanged(() -> notified[0] = true);

        viewModel.saveTitle("New Title");

        assertTrue(notified[0]);
    }

    @Test
    @DisplayName("saveText notifies data changed")
    void saveText_shouldNotifyDataChanged() {
        Note note = noteService.createNote("Title", "Content");
        viewModel.setNote(note.getId());
        boolean[] notified = {false};
        viewModel.setOnDataChanged(() -> notified[0] = true);

        viewModel.saveText("New Content");

        assertTrue(notified[0]);
    }

    @Test
    @DisplayName("saveAttribute notifies data changed")
    void saveAttribute_shouldNotifyDataChanged() {
        Note note = noteService.createNote("Title", "Content");
        viewModel.setNote(note.getId());
        boolean[] notified = {false};
        viewModel.setOnDataChanged(() -> notified[0] = true);

        viewModel.saveAttribute("$Prototype", "Value");

        assertTrue(notified[0]);
    }

    @Test
    @DisplayName("saveAttribute parses number values correctly")
    void saveAttribute_shouldParseNumberValues() {
        Note note = noteService.createNote("Title", "Content");
        viewModel.setNote(note.getId());

        viewModel.saveAttribute("$Width", "12.5");

        Note reloaded = noteService.getNote(note.getId()).orElseThrow();
        AttributeValue.NumberValue val = (AttributeValue.NumberValue)
                reloaded.getAttribute("$Width").orElseThrow();
        assertEquals(12.5, val.value(), 0.001);
    }

    @Test
    @DisplayName("saveAttribute handles boolean values correctly")
    void saveAttribute_shouldHandleBooleanValues() {
        Note note = noteService.createNote("Title", "Content");
        viewModel.setNote(note.getId());

        viewModel.saveAttribute("$Checked", "true");

        Note reloaded = noteService.getNote(note.getId()).orElseThrow();
        AttributeValue.BooleanValue val = (AttributeValue.BooleanValue)
                reloaded.getAttribute("$Checked").orElseThrow();
        assertTrue(val.value());
    }

    @Test
    @DisplayName("setNote displays color attribute as hex string")
    void setNote_shouldDisplayColorAttribute() {
        Note note = noteService.createNote("Title", "Content");
        note.setAttribute("$Color",
                new AttributeValue.ColorValue(TbxColor.hex("#FF0000")));

        viewModel.setNote(note.getId());

        AttributeEntry entry = viewModel.getEditableAttributes().stream()
                .filter(a -> "$Color".equals(a.getName()))
                .findFirst().orElse(null);
        assertNotNull(entry);
        assertEquals("#FF0000", entry.getValue());
    }

    @Test
    @DisplayName("setNote displays url attribute value")
    void setNote_shouldDisplayUrlAttribute() {
        Note note = noteService.createNote("Title", "Content");
        note.setAttribute("$URL",
                new AttributeValue.UrlValue("https://example.com"));

        viewModel.setNote(note.getId());

        AttributeEntry entry = viewModel.getEditableAttributes().stream()
                .filter(a -> "$URL".equals(a.getName()))
                .findFirst().orElse(null);
        assertNotNull(entry);
        assertEquals("https://example.com", entry.getValue());
    }

    @Test
    @DisplayName("setNote displays set attribute as semicolon-delimited")
    void setNote_shouldDisplaySetAttribute() {
        Note note = noteService.createNote("Title", "Content");
        note.setAttribute("$Flags",
                new AttributeValue.SetValue(Set.of("a")));

        viewModel.setNote(note.getId());

        AttributeEntry entry = viewModel.getEditableAttributes().stream()
                .filter(a -> "$Flags".equals(a.getName()))
                .findFirst().orElse(null);
        assertNotNull(entry);
        assertEquals("a", entry.getValue());
    }

    @Test
    @DisplayName("setNote displays list attribute as semicolon-delimited")
    void setNote_shouldDisplayListAttribute() {
        Note note = noteService.createNote("Title", "Content");
        note.setAttribute("$MyList",
                new AttributeValue.ListValue(List.of("x", "y")));

        viewModel.setNote(note.getId());

        AttributeEntry entry = viewModel.getEditableAttributes().stream()
                .filter(a -> "$MyList".equals(a.getName()))
                .findFirst().orElse(null);
        assertNotNull(entry);
        assertEquals("x;y", entry.getValue());
    }

    @Test
    @DisplayName("setNote displays interval attribute")
    void setNote_shouldDisplayIntervalAttribute() {
        Note note = noteService.createNote("Title", "Content");
        note.setAttribute("$MyInterval",
                new AttributeValue.IntervalValue(Duration.ofHours(2)));

        viewModel.setNote(note.getId());

        AttributeEntry entry = viewModel.getEditableAttributes().stream()
                .filter(a -> "$MyInterval".equals(a.getName()))
                .findFirst().orElse(null);
        assertNotNull(entry);
        assertEquals("PT2H", entry.getValue());
    }

    @Test
    @DisplayName("setNote displays file attribute")
    void setNote_shouldDisplayFileAttribute() {
        Note note = noteService.createNote("Title", "Content");
        note.setAttribute("$MyFile",
                new AttributeValue.FileValue("/path/to/file"));

        viewModel.setNote(note.getId());

        AttributeEntry entry = viewModel.getEditableAttributes().stream()
                .filter(a -> "$MyFile".equals(a.getName()))
                .findFirst().orElse(null);
        assertNotNull(entry);
        assertEquals("/path/to/file", entry.getValue());
    }

    @Test
    @DisplayName("setNote displays action attribute")
    void setNote_shouldDisplayActionAttribute() {
        Note note = noteService.createNote("Title", "Content");
        note.setAttribute("$MyAction",
                new AttributeValue.ActionValue("doSomething()"));

        viewModel.setNote(note.getId());

        AttributeEntry entry = viewModel.getEditableAttributes().stream()
                .filter(a -> "$MyAction".equals(a.getName()))
                .findFirst().orElse(null);
        assertNotNull(entry);
        assertEquals("doSomething()", entry.getValue());
    }

    @Test
    @DisplayName("saveAttribute with invalid number falls back to 0")
    void saveAttribute_shouldHandleInvalidNumber() {
        Note note = noteService.createNote("Title", "Content");
        viewModel.setNote(note.getId());

        viewModel.saveAttribute("$Width", "not-a-number");

        Note reloaded = noteService.getNote(note.getId()).orElseThrow();
        AttributeValue.NumberValue val = (AttributeValue.NumberValue)
                reloaded.getAttribute("$Width").orElseThrow();
        assertEquals(0.0, val.value(), 0.001);
    }

    @Test
    @DisplayName("saveAttribute with null value does nothing")
    void saveAttribute_shouldDoNothingForNullValue() {
        Note note = noteService.createNote("Title", "Content");
        viewModel.setNote(note.getId());

        viewModel.saveAttribute("$Prototype", null);
        // Should not throw
    }

    @Test
    @DisplayName("saveAttribute with null name does nothing")
    void saveAttribute_shouldDoNothingForNullName() {
        Note note = noteService.createNote("Title", "Content");
        viewModel.setNote(note.getId());

        viewModel.saveAttribute(null, "value");
        // Should not throw
    }

    @Test
    @DisplayName("saveText with null does nothing")
    void saveText_shouldDoNothingForNull() {
        Note note = noteService.createNote("Title", "Content");
        viewModel.setNote(note.getId());

        viewModel.saveText(null);

        assertEquals("Content", viewModel.textProperty().get());
    }

    @Test
    @DisplayName("saveTitle with null does nothing")
    void saveTitle_shouldDoNothingForNull() {
        Note note = noteService.createNote("Title", "Content");
        viewModel.setNote(note.getId());

        viewModel.saveTitle(null);

        assertEquals("Title", viewModel.titleProperty().get());
    }

    @Test
    @DisplayName("setNote with unknown attribute uses STRING type")
    void setNote_shouldUseStringTypeForUnknownAttribute() {
        Note note = noteService.createNote("Title", "Content");
        note.setAttribute("$CustomUnknown",
                new AttributeValue.StringValue("custom"));

        viewModel.setNote(note.getId());

        AttributeEntry entry = viewModel.getEditableAttributes().stream()
                .filter(a -> "$CustomUnknown".equals(a.getName()))
                .findFirst().orElse(null);
        assertNotNull(entry);
        assertEquals(AttributeType.STRING, entry.getType());
    }

    @Test
    @DisplayName("saveAttribute for unknown attribute saves as string")
    void saveAttribute_shouldSaveUnknownAsString() {
        Note note = noteService.createNote("Title", "Content");
        viewModel.setNote(note.getId());

        viewModel.saveAttribute("$UnknownAttr", "hello");

        Note reloaded = noteService.getNote(note.getId()).orElseThrow();
        AttributeValue.StringValue val = (AttributeValue.StringValue)
                reloaded.getAttribute("$UnknownAttr").orElseThrow();
        assertEquals("hello", val.value());
    }
}
