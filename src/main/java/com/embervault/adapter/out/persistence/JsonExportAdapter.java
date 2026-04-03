package com.embervault.adapter.out.persistence;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.embervault.domain.AttributeValue;
import com.embervault.domain.Link;
import com.embervault.domain.Note;
import com.embervault.domain.Stamp;

/**
 * Exports project data to a full-fidelity JSON file.
 *
 * <p>Produces a JSON document with top-level keys {@code "notes"},
 * {@code "links"}, and {@code "stamps"}, preserving all typed
 * attribute values for round-trip fidelity.</p>
 */
public final class JsonExportAdapter {

    /**
     * Exports notes, links, and stamps to a JSON file.
     *
     * @param notes   the notes to export
     * @param links   the links to export
     * @param stamps  the stamps to export
     * @param output  the output file path
     * @throws IOException if writing fails
     */
    public void export(List<Note> notes, List<Link> links,
            List<Stamp> stamps, Path output) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"notes\": [\n");
        for (int i = 0; i < notes.size(); i++) {
            writeNote(sb, notes.get(i));
            if (i < notes.size() - 1) {
                sb.append(",");
            }
            sb.append("\n");
        }
        sb.append("  ],\n");

        sb.append("  \"links\": [\n");
        for (int i = 0; i < links.size(); i++) {
            writeLink(sb, links.get(i));
            if (i < links.size() - 1) {
                sb.append(",");
            }
            sb.append("\n");
        }
        sb.append("  ],\n");

        sb.append("  \"stamps\": [\n");
        for (int i = 0; i < stamps.size(); i++) {
            writeStamp(sb, stamps.get(i));
            if (i < stamps.size() - 1) {
                sb.append(",");
            }
            sb.append("\n");
        }
        sb.append("  ]\n");
        sb.append("}");

        Files.writeString(output, sb.toString(),
                StandardCharsets.UTF_8);
    }

    private void writeNote(StringBuilder sb, Note note) {
        sb.append("    {\n");
        sb.append("      \"id\": ")
                .append(jsonString(note.getId().toString()))
                .append(",\n");

        note.getPrototypeId().ifPresent(pid ->
                sb.append("      \"prototypeId\": ")
                        .append(jsonString(pid.toString()))
                        .append(",\n"));

        sb.append("      \"attributes\": {\n");
        Map<String, AttributeValue> entries =
                note.getAttributes().localEntries();
        int count = 0;
        for (Map.Entry<String, AttributeValue> entry
                : entries.entrySet()) {
            sb.append("        ")
                    .append(jsonString(entry.getKey()))
                    .append(": ")
                    .append(formatValue(entry.getValue()));
            count++;
            if (count < entries.size()) {
                sb.append(",");
            }
            sb.append("\n");
        }
        sb.append("      }\n");
        sb.append("    }");
    }

    private void writeLink(StringBuilder sb, Link link) {
        sb.append("    {\n");
        sb.append("      \"id\": ")
                .append(jsonString(link.id().toString()))
                .append(",\n");
        sb.append("      \"sourceId\": ")
                .append(jsonString(link.sourceId().toString()))
                .append(",\n");
        sb.append("      \"destinationId\": ")
                .append(jsonString(
                        link.destinationId().toString()))
                .append(",\n");
        sb.append("      \"type\": ")
                .append(jsonString(link.type()))
                .append("\n");
        sb.append("    }");
    }

    private void writeStamp(StringBuilder sb, Stamp stamp) {
        sb.append("    {\n");
        sb.append("      \"id\": ")
                .append(jsonString(stamp.id().toString()))
                .append(",\n");
        sb.append("      \"name\": ")
                .append(jsonString(stamp.name()))
                .append(",\n");
        sb.append("      \"action\": ")
                .append(jsonString(stamp.action()))
                .append("\n");
        sb.append("    }");
    }

    private String formatValue(AttributeValue value) {
        return switch (value) {
            case AttributeValue.StringValue sv ->
                    jsonString(sv.value());
            case AttributeValue.NumberValue nv -> {
                double d = nv.value();
                yield d == Math.floor(d)
                        && !Double.isInfinite(d)
                        ? String.valueOf((long) d)
                        : String.valueOf(d);
            }
            case AttributeValue.BooleanValue bv ->
                    String.valueOf(bv.value());
            case AttributeValue.ColorValue cv ->
                    jsonString(cv.value().toHex());
            case AttributeValue.DateValue dv ->
                    jsonString(dv.value().toString());
            case AttributeValue.IntervalValue iv ->
                    jsonString(iv.value().toString());
            case AttributeValue.FileValue fv ->
                    jsonString(fv.path());
            case AttributeValue.UrlValue uv ->
                    jsonString(uv.url());
            case AttributeValue.ListValue lv ->
                    formatList(lv.values());
            case AttributeValue.SetValue sv ->
                    formatSet(sv.values());
            case AttributeValue.ActionValue av ->
                    jsonString(av.expression());
        };
    }

    private String formatList(java.util.List<String> values) {
        return values.stream()
                .map(this::jsonString)
                .collect(Collectors.joining(", ", "[", "]"));
    }

    private String formatSet(java.util.Set<String> values) {
        return new TreeSet<>(values).stream()
                .map(this::jsonString)
                .collect(Collectors.joining(", ", "[", "]"));
    }

    private String jsonString(String value) {
        return "\"" + value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t") + "\"";
    }
}
