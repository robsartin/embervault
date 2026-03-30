package com.embervault.adapter.out.persistence;

import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.embervault.domain.AttributeValue;
import com.embervault.domain.Attributes;
import com.embervault.domain.Note;

/**
 * Serializes a {@link Note} to a Markdown string with YAML front matter.
 *
 * <p>The {@code $Text} attribute becomes the Markdown body after the
 * closing {@code ---} delimiter. All other attributes are written as
 * YAML key-value pairs in the front matter.</p>
 */
public final class NoteFileSerializer {

    private static final String FRONT_MATTER_DELIMITER = "---\n";

    /**
     * Serializes a note to a Markdown string with YAML front matter.
     *
     * @param note the note to serialize
     * @return the serialized string
     */
    public String serialize(Note note) {
        StringBuilder sb = new StringBuilder();
        sb.append(FRONT_MATTER_DELIMITER);

        // id field
        sb.append("id: \"").append(note.getId()).append("\"\n");

        // prototypeId if set
        note.getPrototypeId().ifPresent(pid ->
                sb.append("prototypeId: \"")
                        .append(pid).append("\"\n"));

        // All attributes except $Text
        String text = null;
        for (Map.Entry<String, AttributeValue> entry
                : note.getAttributes().localEntries().entrySet()) {
            String key = entry.getKey();
            AttributeValue value = entry.getValue();
            if (Attributes.TEXT.equals(key)) {
                text = ((AttributeValue.StringValue) value)
                        .value();
                continue;
            }
            sb.append(key).append(": ")
                    .append(formatValue(value)).append('\n');
        }

        sb.append(FRONT_MATTER_DELIMITER);

        // Markdown body from $Text
        if (text != null && !text.isEmpty()) {
            sb.append(text).append('\n');
        }

        return sb.toString();
    }

    private String formatValue(AttributeValue value) {
        return switch (value) {
            case AttributeValue.StringValue sv ->
                    quote(sv.value());
            case AttributeValue.NumberValue nv -> {
                double d = nv.value();
                yield d == Math.floor(d) && !Double.isInfinite(d)
                        ? String.valueOf((long) d)
                        : String.valueOf(d);
            }
            case AttributeValue.BooleanValue bv ->
                    String.valueOf(bv.value());
            case AttributeValue.ColorValue cv ->
                    quote(cv.value().toHex());
            case AttributeValue.DateValue dv ->
                    quote(dv.value().toString());
            case AttributeValue.IntervalValue iv ->
                    quote(iv.value().toString());
            case AttributeValue.FileValue fv ->
                    quote(fv.path());
            case AttributeValue.UrlValue uv ->
                    quote(uv.url());
            case AttributeValue.ListValue lv ->
                    formatList(lv.values());
            case AttributeValue.SetValue sv ->
                    formatSet(sv.values());
            case AttributeValue.ActionValue av ->
                    quote(av.expression());
        };
    }

    private String formatList(java.util.List<String> values) {
        return values.stream()
                .map(this::quote)
                .collect(Collectors.joining(", ", "[", "]"));
    }

    private String formatSet(java.util.Set<String> values) {
        return new TreeSet<>(values).stream()
                .map(this::quote)
                .collect(Collectors.joining(", ", "[", "]"));
    }

    private String quote(String value) {
        return "\"" + value.replace("\\", "\\\\")
                .replace("\"", "\\\"") + "\"";
    }
}
