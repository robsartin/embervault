package com.embervault.adapter.in.ui.view;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.embervault.adapter.in.ui.viewmodel.SelectedNoteViewModel;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.VBox;
import org.fxmisc.richtext.StyleClassedTextArea;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

/**
 * FXML controller for the Text Pane view.
 *
 * <p>Uses a RichTextFX {@link StyleClassedTextArea} for Markdown syntax
 * highlighting. Binds to {@link SelectedNoteViewModel} for note
 * selection and persistence.</p>
 */
public class TextPaneViewController {

    @FXML private VBox textPaneRoot;
    @FXML private Label placeholderLabel;
    @FXML private TextField titleField;

    private StyleClassedTextArea editor;
    private SelectedNoteViewModel viewModel;
    private boolean suppressTextSync;

    /**
     * Called by FXML after loading. Creates the rich text editor
     * programmatically (RichTextFX controls aren't FXML-native).
     */
    @FXML
    void initialize() {
        editor = new StyleClassedTextArea();
        editor.setWrapText(true);
        editor.getStyleClass().add("markdown-editor");
        editor.getStylesheets().add(getClass().getResource(
                "markdown-editor.css").toExternalForm());
        editor.setVisible(false);
        editor.setManaged(false);
        VBox.setVgrow(editor, javafx.scene.layout.Priority.ALWAYS);
        textPaneRoot.getChildren().add(editor);

        editor.textProperty().addListener(
                (obs, oldVal, newVal) -> applyHighlighting());

        wireFormatShortcuts();
    }

    /**
     * Injects the ViewModel and binds UI controls.
     *
     * @param viewModel the selected note view model
     */
    public void initViewModel(SelectedNoteViewModel viewModel) {
        this.viewModel = viewModel;

        viewModel.selectedNoteIdProperty().addListener(
                (obs, oldVal, newVal) ->
                        updateVisibility(newVal != null));
        updateVisibility(
                viewModel.selectedNoteIdProperty().get() != null);

        titleField.setText(viewModel.titleProperty().get());
        viewModel.titleProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (!titleField.getText().equals(newVal)) {
                        titleField.setText(newVal);
                    }
                });

        titleField.focusedProperty().addListener(
                (obs, wasFocused, isFocused) -> {
                    if (!isFocused) {
                        viewModel.saveTitle(titleField.getText());
                    }
                });

        titleField.setOnAction(
                e -> viewModel.saveTitle(titleField.getText()));

        setEditorText(viewModel.textProperty().get());
        viewModel.textProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (!suppressTextSync
                            && !editor.getText().equals(newVal)) {
                        setEditorText(newVal);
                    }
                });

        editor.focusedProperty().addListener(
                (obs, wasFocused, isFocused) -> {
                    if (!isFocused) {
                        suppressTextSync = true;
                        viewModel.saveText(editor.getText());
                        suppressTextSync = false;
                    }
                });
    }

    /** Returns the associated ViewModel. */
    public SelectedNoteViewModel getViewModel() {
        return viewModel;
    }

    private void setEditorText(String text) {
        editor.replaceText(text != null ? text : "");
        applyHighlighting();
    }

    private void applyHighlighting() {
        String text = editor.getText();
        if (text.isEmpty()) {
            return;
        }
        List<MarkdownSyntaxHighlighter.StyleSpan> spans =
                MarkdownEditorHelper.computeHighlighting(text);
        editor.setStyleSpans(0, buildStyleSpans(text, spans));
    }

    private StyleSpans<Collection<String>> buildStyleSpans(
            String text,
            List<MarkdownSyntaxHighlighter.StyleSpan> spans) {
        StyleSpansBuilder<Collection<String>> builder =
                new StyleSpansBuilder<>();
        int lastEnd = 0;
        // Sort by start position
        List<MarkdownSyntaxHighlighter.StyleSpan> sorted =
                spans.stream()
                        .sorted((a, b) -> Integer.compare(
                                a.start(), b.start()))
                        .toList();
        for (MarkdownSyntaxHighlighter.StyleSpan span : sorted) {
            if (span.start() < lastEnd) {
                continue; // skip overlapping spans
            }
            if (span.start() > lastEnd) {
                builder.add(Collections.emptyList(),
                        span.start() - lastEnd);
            }
            builder.add(List.of(span.styleClass()),
                    span.end() - span.start());
            lastEnd = span.end();
        }
        if (lastEnd < text.length()) {
            builder.add(Collections.emptyList(),
                    text.length() - lastEnd);
        }
        return builder.create();
    }

    private void wireFormatShortcuts() {
        KeyCodeCombination boldCombo = new KeyCodeCombination(
                KeyCode.B, KeyCombination.SHORTCUT_DOWN);
        KeyCodeCombination italicCombo = new KeyCodeCombination(
                KeyCode.I, KeyCombination.SHORTCUT_DOWN);
        KeyCodeCombination codeCombo = new KeyCodeCombination(
                KeyCode.K, KeyCombination.SHORTCUT_DOWN);

        editor.setOnKeyPressed(event -> {
            if (boldCombo.match(event)) {
                applyFormat(MarkdownEditorHelper::applyBold);
                event.consume();
            } else if (italicCombo.match(event)) {
                applyFormat(MarkdownEditorHelper::applyItalic);
                event.consume();
            } else if (codeCombo.match(event)) {
                applyFormat(MarkdownEditorHelper::applyCode);
                event.consume();
            }
        });
    }

    private void applyFormat(FormatFunction fn) {
        String text = editor.getText();
        int selStart = editor.getSelection().getStart();
        int selEnd = editor.getSelection().getEnd();
        if (selStart == selEnd) {
            return;
        }
        MarkdownFormatter.FormatResult result =
                fn.apply(text, selStart, selEnd);
        editor.replaceText(result.text());
        editor.selectRange(result.selectionStart(),
                result.selectionEnd());
    }

    @FunctionalInterface
    private interface FormatFunction {
        MarkdownFormatter.FormatResult apply(
                String text, int selStart, int selEnd);
    }

    private void updateVisibility(boolean noteSelected) {
        placeholderLabel.setVisible(!noteSelected);
        placeholderLabel.setManaged(!noteSelected);
        titleField.setVisible(noteSelected);
        titleField.setManaged(noteSelected);
        editor.setVisible(noteSelected);
        editor.setManaged(noteSelected);
    }
}
