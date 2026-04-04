package com.embervault;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.embervault.adapter.in.ui.view.StampEditorViewController;
import com.embervault.adapter.in.ui.viewmodel.StampEditorViewModel;
import com.embervault.adapter.out.persistence.JsonExportAdapter;
import com.embervault.adapter.out.persistence.MarkdownExportAdapter;
import com.embervault.adapter.out.persistence.OpmlExportAdapter;
import com.embervault.adapter.out.persistence.ProjectFileManager;
import com.embervault.application.ExportServiceImpl;
import com.embervault.application.port.in.ExportProjectUseCase;
import com.embervault.application.port.in.StampService;
import com.embervault.domain.ExportFormat;
import com.embervault.domain.Stamp;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates menu bars for application windows.
 *
 * <p>Both the primary window and secondary windows use this factory
 * to build identical menu bars with Note, Edit, Stamps, and Window
 * menus.</p>
 */
final class MenuBarFactory {

    private static final Logger LOG =
            LoggerFactory.getLogger(MenuBarFactory.class);

    private MenuBarFactory() { }

    static MenuBar create(WindowContext ctx) {
        Menu fileMenu = buildFileMenu(ctx);
        Menu noteMenu = buildNoteMenu(ctx);
        Menu editMenu = buildEditMenu(ctx);
        Menu stampsMenu = new Menu("Stamps");
        buildStampsMenu(stampsMenu, ctx);
        Menu windowMenu = buildWindowMenu(ctx);

        MenuBar menuBar = new MenuBar(
                fileMenu, noteMenu, editMenu,
                stampsMenu, windowMenu);
        menuBar.setUseSystemMenuBar(true);
        return menuBar;
    }

    private static Menu buildFileMenu(WindowContext ctx) {
        MenuItem saveItem = new MenuItem("Save...");
        saveItem.setAccelerator(
                new KeyCodeCombination(KeyCode.S,
                        KeyCombination.SHORTCUT_DOWN));
        saveItem.setOnAction(e -> {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Save Project");
            File dir = chooser.showDialog(ctx.ownerStage());
            if (dir != null) {
                SharedServices svc = ctx.sharedServices();
                ProjectFileManager.save(
                        dir.toPath(),
                        svc.project(),
                        svc.noteService(),
                        svc.linkService(),
                        svc.stampService(),
                        svc.schemaRegistry());
            }
        });

        MenuItem openItem = new MenuItem("Open...");
        openItem.setAccelerator(
                new KeyCodeCombination(KeyCode.O,
                        KeyCombination.SHORTCUT_DOWN));
        openItem.setOnAction(e -> {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Open Project");
            File dir = chooser.showDialog(ctx.ownerStage());
            if (dir != null) {
                SharedServices svc = ctx.sharedServices();
                UUID rootId = ProjectFileManager.load(
                        dir.toPath(),
                        svc.noteRepository(),
                        svc.noteService(),
                        svc.linkService(),
                        svc.stampService(),
                        svc.schemaRegistry());
                if (ctx.onBaseNoteChanged() != null) {
                    ctx.onBaseNoteChanged().accept(rootId);
                }
                ctx.appState().notifyDataChanged();
            }
        });

        Menu exportMenu = buildExportMenu(ctx);

        Menu menu = new Menu("File");
        menu.getItems().addAll(openItem, saveItem,
                new SeparatorMenuItem(), exportMenu);
        return menu;
    }

    private static Menu buildExportMenu(WindowContext ctx) {
        MenuItem jsonItem = new MenuItem("JSON...");
        jsonItem.setOnAction(e ->
                doExport(ctx, ExportFormat.JSON, "JSON",
                        "*.json"));
        MenuItem mdItem = new MenuItem("Markdown...");
        mdItem.setOnAction(e ->
                doExportDir(ctx, ExportFormat.MARKDOWN));
        MenuItem opmlItem = new MenuItem("OPML...");
        opmlItem.setOnAction(e ->
                doExport(ctx, ExportFormat.OPML, "OPML",
                        "*.opml"));
        Menu menu = new Menu("Export");
        menu.getItems().addAll(jsonItem, mdItem, opmlItem);
        return menu;
    }

    private static void doExport(WindowContext ctx,
            ExportFormat format, String desc, String ext) {
        String suffix = ext.replace("*", "");
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export as " + desc);
        chooser.setInitialFileName("export");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(desc, ext));
        File file = chooser.showSaveDialog(ctx.ownerStage());
        if (file != null) {
            if (!file.getName().endsWith(suffix)) {
                file = new File(file.getPath() + suffix);
            }
            try {
                createExportService(ctx)
                        .exportProject(format, file.toPath());
                LOG.info("Exported {} to {}", desc, file);
            } catch (IOException ex) {
                LOG.error("Export to {} failed", file, ex);
            }
        }
    }

    private static void doExportDir(WindowContext ctx,
            ExportFormat format) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Export as Markdown");
        File dir = chooser.showDialog(ctx.ownerStage());
        if (dir != null) {
            try {
                createExportService(ctx)
                        .exportProject(format, dir.toPath());
                LOG.info("Exported Markdown to {}", dir);
            } catch (IOException ex) {
                LOG.error("Export to {} failed", dir, ex);
            }
        }
    }

    private static ExportProjectUseCase createExportService(
            WindowContext ctx) {
        SharedServices svc = ctx.sharedServices();
        return new ExportServiceImpl(
                svc.project(), svc.noteService(),
                svc.linkService(), svc.stampService(),
                Map.of(ExportFormat.JSON,
                        new JsonExportAdapter(),
                        ExportFormat.MARKDOWN,
                        new MarkdownExportAdapter(),
                        ExportFormat.OPML,
                        new OpmlExportAdapter()));
    }

    private static Menu buildNoteMenu(WindowContext ctx) {
        MenuItem createNote = new MenuItem("Create Note");
        createNote.setAccelerator(
                new KeyCodeCombination(KeyCode.N,
                        KeyCombination.SHORTCUT_DOWN));
        createNote.setOnAction(e -> {
            UUID parentId = ctx.selectedNoteId().get();
            if (parentId != null) {
                ctx.sharedServices().noteService()
                        .createChildNote(parentId, "Untitled");
                ctx.appState().notifyDataChanged();
            }
        });
        Menu menu = new Menu("Note");
        menu.getItems().add(createNote);
        return menu;
    }

    private static Menu buildEditMenu(WindowContext ctx) {
        MenuItem undoItem = new MenuItem("Undo");
        undoItem.setAccelerator(
                new KeyCodeCombination(KeyCode.Z,
                        KeyCombination.SHORTCUT_DOWN));
        MenuItem redoItem = new MenuItem("Redo");
        redoItem.setAccelerator(
                new KeyCodeCombination(KeyCode.Z,
                        KeyCombination.SHORTCUT_DOWN,
                        KeyCombination.SHIFT_DOWN));
        if (ctx.commandHistory() != null) {
            var history = ctx.commandHistory();
            undoItem.setOnAction(e -> {
                history.undo();
                ctx.appState().notifyDataChanged();
            });
            redoItem.setOnAction(e -> {
                history.redo();
                ctx.appState().notifyDataChanged();
            });
        }
        undoItem.setDisable(true);
        redoItem.setDisable(true);

        MenuItem findItem = new MenuItem("Find");
        findItem.setAccelerator(
                new KeyCodeCombination(KeyCode.F,
                        KeyCombination.SHORTCUT_DOWN));
        findItem.setOnAction(e -> {
            if (ctx.onFind() != null) {
                ctx.onFind().run();
            }
        });
        Menu menu = new Menu("Edit");
        menu.getItems().addAll(undoItem, redoItem,
                new SeparatorMenuItem(), findItem);
        if (ctx.commandHistory() != null) {
            var history = ctx.commandHistory();
            menu.setOnShowing(e -> {
                undoItem.setDisable(!history.canUndo());
                redoItem.setDisable(!history.canRedo());
            });
        }
        return menu;
    }

    private static Menu buildWindowMenu(WindowContext ctx) {
        MenuItem newWindowItem = new MenuItem("New Window");
        newWindowItem.setAccelerator(
                new KeyCodeCombination(KeyCode.N,
                        KeyCombination.SHORTCUT_DOWN,
                        KeyCombination.SHIFT_DOWN));
        newWindowItem.setOnAction(e -> {
            try {
                WindowFactory.openNewWindow(
                        ctx.sharedServices(),
                        ctx.windowManager());
            } catch (IOException ex) {
                LOG.error("Failed to open new window", ex);
            }
        });
        Menu menu = new Menu("Window");
        menu.getItems().add(newWindowItem);
        return menu;
    }

    private static void buildStampsMenu(
            Menu stampsMenu, WindowContext ctx) {
        stampsMenu.getItems().clear();
        StampService stampService =
                ctx.sharedServices().stampService();

        MenuItem inspectItem = new MenuItem("Inspect Stamps...");
        inspectItem.setOnAction(e -> {
            try {
                openStampEditor(stampsMenu, ctx);
            } catch (IOException ex) {
                LOG.error("Failed to open stamp editor", ex);
            }
        });
        stampsMenu.getItems().add(inspectItem);
        stampsMenu.getItems().add(new SeparatorMenuItem());

        Map<String, Menu> subMenus = new HashMap<>();
        for (Stamp stamp : stampService.getAllStamps()) {
            String name = stamp.name();
            UUID stampId = stamp.id();
            int colonIndex = name.indexOf(':');
            boolean hasSubmenu = colonIndex > 0
                    && colonIndex == name.lastIndexOf(':');
            if (hasSubmenu) {
                String menuName = name.substring(0, colonIndex);
                String itemName = name.substring(colonIndex + 1);
                Menu subMenu = subMenus.computeIfAbsent(menuName,
                        k -> {
                            Menu m = new Menu(k);
                            stampsMenu.getItems().add(m);
                            return m;
                        });
                MenuItem item = new MenuItem(itemName);
                item.setOnAction(ev ->
                        applyStamp(ctx, stampId));
                subMenu.getItems().add(item);
            } else {
                MenuItem item = new MenuItem(name);
                item.setOnAction(ev ->
                        applyStamp(ctx, stampId));
                stampsMenu.getItems().add(item);
            }
        }
    }

    private static void applyStamp(
            WindowContext ctx, UUID stampId) {
        UUID noteId = ctx.selectedNoteId().get();
        if (noteId == null) {
            return;
        }
        try {
            ctx.sharedServices().stampService()
                    .applyStamp(stampId, noteId);
            ctx.appState().notifyDataChanged();
        } catch (Exception ex) {
            LOG.error("Failed to apply stamp", ex);
        }
    }

    private static void openStampEditor(
            Menu stampsMenu, WindowContext ctx)
            throws IOException {
        FXMLLoader loader = new FXMLLoader(
                MenuBarFactory.class.getResource(
                        "/com/embervault/adapter/in/ui/view/"
                                + "StampEditorView.fxml"));
        Parent editorRoot = loader.load();
        StampEditorViewController controller =
                loader.getController();
        StampEditorViewModel editorVm =
                new StampEditorViewModel(
                        ctx.sharedServices().stampService());
        controller.initViewModel(editorVm);
        Stage editorStage = new Stage();
        editorStage.setTitle("Inspect Stamps");
        editorStage.setScene(new Scene(editorRoot));
        editorStage.initOwner(ctx.ownerStage());
        editorStage.showAndWait();
        buildStampsMenu(stampsMenu, ctx);
    }
}
