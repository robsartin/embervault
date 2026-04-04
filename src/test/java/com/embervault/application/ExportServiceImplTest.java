package com.embervault.application;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.embervault.adapter.out.persistence.JsonExportAdapter;
import com.embervault.adapter.out.persistence.MarkdownExportAdapter;
import com.embervault.adapter.out.persistence.OpmlExportAdapter;
import com.embervault.application.port.in.ExportProjectUseCase;
import com.embervault.application.port.in.GetNoteQuery;
import com.embervault.application.port.in.LinkService;
import com.embervault.application.port.in.StampService;
import com.embervault.application.port.out.ExportPort;
import com.embervault.domain.ExportFormat;
import com.embervault.domain.Link;
import com.embervault.domain.Note;
import com.embervault.domain.Project;
import com.embervault.domain.Stamp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for {@link ExportServiceImpl}.
 */
class ExportServiceImplTest {

    @TempDir
    Path tempDir;

    private List<Note> testNotes;
    private List<Link> testLinks;
    private List<Stamp> testStamps;
    private Project project;

    @BeforeEach
    void setUp() {
        project = Project.createEmpty();
        Instant now = Instant.now();
        Note noteA = new Note(UUID.randomUUID(), "NoteA",
                "Body A", now, now);
        Note noteB = new Note(UUID.randomUUID(), "NoteB",
                "Body B", now, now);
        testNotes = List.of(noteA, noteB);
        testLinks = List.of(
                Link.create(noteA.getId(), noteB.getId()));
        testStamps = List.of(
                Stamp.create("Mark Done", "$Checked=true"));
    }

    @Test
    @DisplayName("exports project as JSON")
    void exportJson() throws IOException {
        ExportProjectUseCase service = createService();
        Path output = tempDir.resolve("export.json");

        service.exportProject(ExportFormat.JSON, output);

        assertTrue(Files.exists(output));
        String json = Files.readString(output,
                StandardCharsets.UTF_8);
        assertTrue(json.contains("\"notes\""));
        assertTrue(json.contains("\"NoteA\""));
        assertTrue(json.contains("\"links\""));
        assertTrue(json.contains("\"stamps\""));
        assertTrue(json.contains("\"Mark Done\""));
    }

    @Test
    @DisplayName("exports project as Markdown")
    void exportMarkdown() throws IOException {
        ExportProjectUseCase service = createService();
        Path output = tempDir.resolve("md-export");

        service.exportProject(ExportFormat.MARKDOWN, output);

        assertTrue(Files.exists(
                output.resolve("NoteA.md")));
        assertTrue(Files.exists(
                output.resolve("NoteB.md")));
    }

    @Test
    @DisplayName("exports project as OPML")
    void exportOpml() throws IOException {
        ExportProjectUseCase service = createService();
        Path output = tempDir.resolve("export.opml");

        service.exportProject(ExportFormat.OPML, output);

        assertTrue(Files.exists(output));
        String opml = Files.readString(output,
                StandardCharsets.UTF_8);
        assertTrue(opml.contains("<opml"));
        assertTrue(opml.contains("text=\"NoteA\""));
        assertTrue(opml.contains("text=\"NoteB\""));
    }

    private ExportProjectUseCase createService() {
        GetNoteQuery noteQuery = new GetNoteQuery() {
            @Override
            public Optional<Note> getNote(UUID id) {
                return Optional.empty();
            }

            @Override
            public List<Note> getAllNotes() {
                return testNotes;
            }

            @Override
            public List<Note> getChildren(UUID parentId) {
                return List.of();
            }

            @Override
            public boolean hasChildren(UUID noteId) {
                return false;
            }

            @Override
            public Map<UUID, Boolean> hasChildrenBatch(
                    Collection<UUID> noteIds) {
                return Map.of();
            }
        };

        LinkService linkSvc = new LinkService() {
            @Override
            public Link createLink(UUID src, UUID dst) {
                return null;
            }

            @Override
            public Link createLink(UUID src, UUID dst,
                    String type) {
                return null;
            }

            @Override
            public List<Link> getLinksFrom(UUID noteId) {
                return testLinks;
            }

            @Override
            public List<Link> getLinksTo(UUID noteId) {
                return List.of();
            }

            @Override
            public List<Link> getAllLinksFor(UUID noteId) {
                return testLinks;
            }

            @Override
            public void deleteLink(UUID linkId) { }
        };

        StampService stampSvc = new StampService() {
            @Override
            public Stamp createStamp(String name,
                    String action) {
                return null;
            }

            @Override
            public void deleteStamp(UUID id) { }

            @Override
            public List<Stamp> getAllStamps() {
                return testStamps;
            }

            @Override
            public Optional<Stamp> getStamp(UUID id) {
                return Optional.empty();
            }

            @Override
            public void applyStamp(UUID stampId,
                    UUID noteId) { }
        };

        Map<ExportFormat, ExportPort> ports = Map.of(
                ExportFormat.JSON,
                new JsonExportAdapter(),
                ExportFormat.MARKDOWN,
                new MarkdownExportAdapter(),
                ExportFormat.OPML,
                new OpmlExportAdapter());

        return new ExportServiceImpl(project, noteQuery,
                linkSvc, stampSvc, ports);
    }
}
