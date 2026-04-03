package com.embervault.adapter.out.persistence;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.embervault.application.port.out.ExportPort;
import com.embervault.domain.Link;
import com.embervault.domain.Note;
import com.embervault.domain.Stamp;

/**
 * Exports notes to OPML (Outline Processor Markup Language) format.
 *
 * <p>Produces an OPML 2.0 document where each note becomes an
 * {@code <outline>} element. The note title maps to the
 * {@code text} attribute, and the note body maps to the
 * {@code _note} attribute (a common OPML convention).</p>
 */
public final class OpmlExportAdapter implements ExportPort {

    @Override
    public void export(List<Note> notes, List<Link> links,
            List<Stamp> stamps, String projectName,
            Path output) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<opml version=\"2.0\">\n");
        sb.append("  <head>\n");
        sb.append("    <title>")
                .append(escapeXml(projectName))
                .append("</title>\n");
        sb.append("  </head>\n");
        sb.append("  <body>\n");

        for (Note note : notes) {
            writeOutline(sb, note);
        }

        sb.append("  </body>\n");
        sb.append("</opml>\n");

        Files.writeString(output, sb.toString(),
                StandardCharsets.UTF_8);
    }

    private void writeOutline(StringBuilder sb, Note note) {
        sb.append("    <outline");
        sb.append(" text=\"")
                .append(escapeXml(note.getTitle()))
                .append("\"");

        String text = note.getText();
        if (text != null && !text.isEmpty()) {
            sb.append(" _note=\"")
                    .append(escapeXml(text))
                    .append("\"");
        }

        sb.append(" created=\"")
                .append(note.getCreatedAt().toString())
                .append("\"");

        sb.append("/>\n");
    }

    private String escapeXml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
