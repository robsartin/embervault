package com.embervault.adapter.in.ui.viewmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.embervault.adapter.out.persistence.InMemoryNoteRepository;
import com.embervault.application.NoteServiceImpl;
import com.embervault.application.port.in.NoteService;
import com.embervault.domain.AttributeSchemaRegistry;
import com.embervault.domain.AttributeValue;
import com.embervault.domain.Note;
import com.embervault.domain.TbxColor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AttributeBrowserViewModelTest {

    private AttributeBrowserViewModel viewModel;
    private NoteService noteService;
    private InMemoryNoteRepository repository;
    private AttributeSchemaRegistry schemaRegistry;
    private AppState appState;

    @BeforeEach
    void setUp() {
        repository = new InMemoryNoteRepository();
        noteService = new NoteServiceImpl(repository);
        schemaRegistry = new AttributeSchemaRegistry();
        appState = new AppState();
        viewModel = new AttributeBrowserViewModel(
                noteService, schemaRegistry, appState);
    }

    @Test
    @DisplayName("Constructor rejects null noteService")
    void constructor_shouldRejectNullNoteService() {
        assertThrows(NullPointerException.class,
                () -> new AttributeBrowserViewModel(
                        null, schemaRegistry, appState));
    }

    @Test
    @DisplayName("Constructor rejects null schemaRegistry")
    void constructor_shouldRejectNullSchemaRegistry() {
        assertThrows(NullPointerException.class,
                () -> new AttributeBrowserViewModel(
                        noteService, null, appState));
    }

    @Test
    @DisplayName("tabTitle is 'Browser' initially when no attribute selected")
    void tabTitle_shouldBeBrowserInitially() {
        assertEquals("Browser", viewModel.tabTitleProperty().get());
    }

    @Test
    @DisplayName("tabTitle updates when attribute is selected")
    void tabTitle_shouldUpdateWhenAttributeSelected() {
        viewModel.setSelectedAttribute("$Color");

        assertEquals("Browser: $Color", viewModel.tabTitleProperty().get());
    }

    @Test
    @DisplayName("tabTitle truncates long attribute names")
    void tabTitle_shouldTruncateLongAttributeNames() {
        viewModel.setSelectedAttribute("$VeryLongAttributeNameHere");

        assertEquals("Browser: $VeryLongAttributeNa\u2026",
                viewModel.tabTitleProperty().get());
    }

    @Test
    @DisplayName("getAvailableAttributes returns groupable attribute names")
    void getAvailableAttributes_shouldReturnGroupableAttributes() {
        List<String> available = viewModel.getAvailableAttributes();

        // Should include String, Boolean, Color, Set attributes but not intrinsic ones
        assertTrue(available.contains("$Name"));
        assertTrue(available.contains("$Color"));
        assertTrue(available.contains("$Checked"));
        // Should not include intrinsic attributes like $Xpos, $Ypos
        assertFalse(available.contains("$Xpos"));
        assertFalse(available.contains("$Ypos"));
        assertFalse(available.contains("$Created"));
    }

    @Test
    @DisplayName("groupNotes produces empty categories when no attribute selected")
    void groupNotes_shouldBeEmptyWhenNoAttribute() {
        viewModel.groupNotes();

        assertTrue(viewModel.getCategories().isEmpty());
    }

    @Test
    @DisplayName("groupNotes groups notes by string attribute value")
    void groupNotes_shouldGroupByStringAttribute() {
        Note note1 = noteService.createNote("Note1", "");
        note1.setAttribute("$Prototype", new AttributeValue.StringValue("TypeA"));
        Note note2 = noteService.createNote("Note2", "");
        note2.setAttribute("$Prototype", new AttributeValue.StringValue("TypeB"));
        Note note3 = noteService.createNote("Note3", "");
        note3.setAttribute("$Prototype", new AttributeValue.StringValue("TypeA"));

        viewModel.setSelectedAttribute("$Prototype");

        assertEquals(2, viewModel.getCategories().size());

        CategoryItem typeA = viewModel.getCategories().stream()
                .filter(c -> "TypeA".equals(c.value()))
                .findFirst().orElse(null);
        assertNotNull(typeA);
        assertEquals(2, typeA.count());

        CategoryItem typeB = viewModel.getCategories().stream()
                .filter(c -> "TypeB".equals(c.value()))
                .findFirst().orElse(null);
        assertNotNull(typeB);
        assertEquals(1, typeB.count());
    }

    @Test
    @DisplayName("groupNotes places notes with no value under (none)")
    void groupNotes_shouldPlaceNoValueUnderNone() {
        noteService.createNote("Note1", "");
        Note note2 = noteService.createNote("Note2", "");
        note2.setAttribute("$Prototype", new AttributeValue.StringValue("TypeA"));

        viewModel.setSelectedAttribute("$Prototype");

        CategoryItem noneCategory = viewModel.getCategories().stream()
                .filter(c -> "(none)".equals(c.value()))
                .findFirst().orElse(null);
        assertNotNull(noneCategory);
        assertEquals(1, noneCategory.count());
    }

    @Test
    @DisplayName("groupNotes handles boolean attributes")
    void groupNotes_shouldGroupByBooleanAttribute() {
        Note note1 = noteService.createNote("Note1", "");
        note1.setAttribute("$Checked", new AttributeValue.BooleanValue(true));
        Note note2 = noteService.createNote("Note2", "");
        note2.setAttribute("$Checked", new AttributeValue.BooleanValue(false));

        viewModel.setSelectedAttribute("$Checked");

        // Notes without $Checked set go to (none) or their default
        assertTrue(viewModel.getCategories().size() >= 2);
    }

    @Test
    @DisplayName("groupNotes handles set attributes with multiple values")
    void groupNotes_shouldHandleSetAttributes() {
        Note note1 = noteService.createNote("Note1", "");
        note1.setAttribute("$Flags",
                new AttributeValue.SetValue(Set.of("flagA", "flagB")));
        Note note2 = noteService.createNote("Note2", "");
        note2.setAttribute("$Flags",
                new AttributeValue.SetValue(Set.of("flagB", "flagC")));

        viewModel.setSelectedAttribute("$Flags");

        // note1 should appear under both flagA and flagB
        // note2 should appear under both flagB and flagC
        CategoryItem flagB = viewModel.getCategories().stream()
                .filter(c -> "flagB".equals(c.value()))
                .findFirst().orElse(null);
        assertNotNull(flagB);
        assertEquals(2, flagB.count(), "Both notes should appear under flagB");
    }

    @Test
    @DisplayName("groupNotes handles color attribute")
    void groupNotes_shouldGroupByColorAttribute() {
        Note note1 = noteService.createNote("Note1", "");
        note1.setAttribute("$Color",
                new AttributeValue.ColorValue(TbxColor.hex("#FF0000")));
        Note note2 = noteService.createNote("Note2", "");
        note2.setAttribute("$Color",
                new AttributeValue.ColorValue(TbxColor.hex("#00FF00")));

        viewModel.setSelectedAttribute("$Color");

        assertTrue(viewModel.getCategories().size() >= 2);
    }

    @Test
    @DisplayName("selectNote sets selectedNoteId")
    void selectNote_shouldSetSelectedNoteId() {
        UUID noteId = UUID.randomUUID();

        viewModel.selectNote(noteId);

        assertEquals(noteId, viewModel.selectedNoteIdProperty().get());
    }

    @Test
    @DisplayName("selectNote(null) clears selection")
    void selectNote_null_shouldClearSelection() {
        viewModel.selectNote(UUID.randomUUID());

        viewModel.selectNote(null);

        assertNull(viewModel.selectedNoteIdProperty().get());
    }

    @Test
    @DisplayName("setSelectedAttribute stores and returns the selected attribute")
    void setSelectedAttribute_shouldStoreAttribute() {
        viewModel.setSelectedAttribute("$Color");

        assertEquals("$Color", viewModel.getSelectedAttribute());
    }

    @Test
    @DisplayName("setSelectedAttribute regroups notes")
    void setSelectedAttribute_shouldRegroupNotes() {
        Note note1 = noteService.createNote("Note1", "");
        note1.setAttribute("$Prototype", new AttributeValue.StringValue("A"));

        viewModel.setSelectedAttribute("$Prototype");

        assertFalse(viewModel.getCategories().isEmpty());
    }

    @Test
    @DisplayName("categories contain NoteDisplayItems with correct titles")
    void categories_shouldContainCorrectNoteDisplayItems() {
        Note note1 = noteService.createNote("MyNote", "some content");
        note1.setAttribute("$Prototype", new AttributeValue.StringValue("TypeA"));

        viewModel.setSelectedAttribute("$Prototype");

        CategoryItem typeA = viewModel.getCategories().stream()
                .filter(c -> "TypeA".equals(c.value()))
                .findFirst().orElse(null);
        assertNotNull(typeA);
        assertEquals(1, typeA.notes().size());
        assertEquals("MyNote", typeA.notes().get(0).getTitle());
    }

    @Test
    @DisplayName("groupNotes handles empty string attribute value as (none)")
    void groupNotes_shouldTreatEmptyStringAsNone() {
        Note note1 = noteService.createNote("Note1", "");
        note1.setAttribute("$Prototype", new AttributeValue.StringValue(""));

        viewModel.setSelectedAttribute("$Prototype");

        CategoryItem noneCategory = viewModel.getCategories().stream()
                .filter(c -> "(none)".equals(c.value()))
                .findFirst().orElse(null);
        assertNotNull(noneCategory);
    }

    @Test
    @DisplayName("groupNotes handles empty set as (none)")
    void groupNotes_shouldHandleEmptySet() {
        Note note1 = noteService.createNote("Note1", "");
        note1.setAttribute("$Flags",
                new AttributeValue.SetValue(Set.of()));

        viewModel.setSelectedAttribute("$Flags");

        CategoryItem noneCategory = viewModel.getCategories().stream()
                .filter(c -> "(none)".equals(c.value()))
                .findFirst().orElse(null);
        assertNotNull(noneCategory);
    }

    @Test
    @DisplayName("setSelectedAttribute to null clears categories")
    void setSelectedAttribute_null_shouldClearCategories() {
        noteService.createNote("Note1", "");
        viewModel.setSelectedAttribute("$Name");
        assertFalse(viewModel.getCategories().isEmpty());

        viewModel.setSelectedAttribute(null);

        assertTrue(viewModel.getCategories().isEmpty());
    }

    @Test
    @DisplayName("tabTitle shows Browser when attribute set to null")
    void tabTitle_shouldShowBrowserWhenAttributeNull() {
        viewModel.setSelectedAttribute("$Color");
        viewModel.setSelectedAttribute(null);

        assertEquals("Browser", viewModel.tabTitleProperty().get());
    }

    @Test
    @DisplayName("groupNotes includes NoteDisplayItem with color hex")
    void groupNotes_shouldIncludeColorHexInDisplayItems() {
        Note note1 = noteService.createNote("Note1", "");
        note1.setAttribute("$Color",
                new AttributeValue.ColorValue(TbxColor.hex("#00FF00")));
        note1.setAttribute("$Prototype",
                new AttributeValue.StringValue("TypeA"));

        viewModel.setSelectedAttribute("$Prototype");

        CategoryItem typeA = viewModel.getCategories().stream()
                .filter(c -> "TypeA".equals(c.value()))
                .findFirst().orElse(null);
        assertNotNull(typeA);
        assertEquals("#00FF00", typeA.notes().get(0).getColorHex());
    }
}
