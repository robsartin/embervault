package com.embervault;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.embervault.adapter.in.ui.view.StampEditorViewController;
import com.embervault.adapter.in.ui.viewmodel.StampEditorViewModel;
import com.embervault.application.port.in.StampService;
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
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates menu bars for application windows.
 *
 * <p>Both the primary window ({@link App}) and secondary windows
 * ({@link WindowFactory}) use this factory to build identical
 * menu bars with Note, Edit, Stamps, and Window menus.</p>
 */
final class MenuBarFactory {

    private static final Logger LOG =
            LoggerFactory.getLogger(MenuBarFactory.class);

    private MenuBarFactory() { }

    /**
     * Creates a menu bar for a window.
     *
     * @param ctx the window context with per-window state
     * @return a fully wired MenuBar
     */
    static MenuBar create(WindowContext ctx) {
        // Note menu
        MenuItem createNote = new MenuItem("Create Note");
        createNote.setAccelerator(
                new KeyCodeCombination(KeyCode.N,
                        KeyCombination.SHORTCUT_DOWN));
        createNote.setOnAction(e -> {
            UUID parentId = ctx.selectedNoteId().get();
            if (parentId != null) {
                ctx.sharedServices().noteService()
                        .createChildNote(parentId, "Untitled");
                ctx.refreshAll().run();
            }
        });
        Menu noteMenu = new Menu("Note");
        noteMenu.getItems().add(createNote);

        // Stamps menu
        Menu stampsMenu = new Menu("Stamps");
        buildStampsMenu(stampsMenu, ctx);

        // Edit menu
        MenuItem findItem = new MenuItem("Find");
        findItem.setAccelerator(
                new KeyCodeCombination(KeyCode.F,
                        KeyCombination.SHORTCUT_DOWN));
        findItem.setOnAction(e -> {
            if (ctx.onFind() != null) {
                ctx.onFind().run();
            }
        });
        Menu editMenu = new Menu("Edit");
        editMenu.getItems().add(findItem);

        // Window menu
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
        Menu windowMenu = new Menu("Window");
        windowMenu.getItems().add(newWindowItem);

        MenuBar menuBar = new MenuBar(
                noteMenu, editMenu, stampsMenu, windowMenu);
        menuBar.setUseSystemMenuBar(true);
        return menuBar;
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
            LOG.warn("No note selected to apply stamp to");
            return;
        }
        try {
            ctx.sharedServices().stampService()
                    .applyStamp(stampId, noteId);
            ctx.refreshAll().run();
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
