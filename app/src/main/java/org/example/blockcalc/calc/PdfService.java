package org.example.blockcalc.calc;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.example.blockcalc.calc.Views.CalcDetail;
import org.example.blockcalc.common.Times;
import org.springframework.context.MessageSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.ZoneId;
import java.util.Locale;

/** Renders a calculation report. Uses an embedded DejaVu Sans font so Cyrillic text works. */
@Service
public class PdfService {

    private final MessageSource messages;
    private final byte[] fontBytes;

    public PdfService(MessageSource messages) throws IOException {
        this.messages = messages;
        try (InputStream in = new ClassPathResource("fonts/DejaVuSans.ttf").getInputStream()) {
            this.fontBytes = in.readAllBytes();
        }
    }

    /** Times are printed in the time zone the browser reported (GMT when unknown). */
    public byte[] render(CalcDetail d, Locale locale, ZoneId zone) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 40, 40, 40, 40);
        try {
            PdfWriter.getInstance(doc, out);
            doc.open();
            BaseFont base = BaseFont.createFont("DejaVuSans.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, true, fontBytes, null);
            Font normal = new Font(base, 10);
            Font title = new Font(base, 18);
            Font section = new Font(base, 12);
            Font small = new Font(base, 9, Font.NORMAL, new Color(110, 120, 108));

            Paragraph heading = new Paragraph(d.title(), title);
            heading.setSpacingAfter(4);
            doc.add(heading);
            if (d.frozen()) {
                Paragraph frozen = new Paragraph(msg(locale, "calc.frozen"), small);
                frozen.setSpacingAfter(4);
                doc.add(frozen);
            }

            PdfPTable info = table(2, new float[]{35, 65}, 10, 0);
            row(info, normal, msg(locale, "calc.project"), d.projectSystem() ? msg(locale, "project.system") : d.projectName());
            row(info, normal, msg(locale, "calc.author"), d.author());
            row(info, normal, msg(locale, "calc.created"), Times.formatIn(d.createdAt(), zone));
            row(info, normal, msg(locale, "calc.updatedBy"), d.updatedBy());
            row(info, normal, msg(locale, "calc.updated"), Times.formatIn(d.updatedAt(), zone));
            doc.add(info);

            doc.add(sectionTitle(msg(locale, "calc.input"), section));
            PdfPTable input = table(2, new float[]{35, 65}, 0, 0);
            row(input, normal, msg(locale, "calc.fullBlocks"), String.valueOf(d.fullBlocks()));
            row(input, normal, msg(locale, "calc.halfBlocks"), String.valueOf(d.halfBlocks()));
            row(input, normal, msg(locale, "calc.batch"), String.valueOf(d.batch()));
            row(input, normal, msg(locale, "calc.ws"), d.ws().stripTrailingZeros().toPlainString() + " " + msg(locale, "unit.kg"));
            row(input, normal, msg(locale, "calc.roundUp"), msg(locale, d.roundUp() ? "common.yes" : "common.no"));
            doc.add(input);

            CalcResult r = d.result();
            doc.add(sectionTitle(msg(locale, "calc.result"), section));
            PdfPTable res = table(3, new float[]{40, 30, 30}, 0, 0);
            header(res, normal, msg(locale, "calc.material"), msg(locale, "calc.quantityKg"),
                    msg(locale, "calc.cost") + ", " + r.currency());
            res.addCell(cell(msg(locale, "material.cement"), normal, false));
            res.addCell(cell(r.cementKg().toPlainString(), normal, true));
            res.addCell(cell(r.cementCost().toPlainString(), normal, true));
            res.addCell(cell(msg(locale, "material.stone"), normal, false));
            res.addCell(cell(r.stoneKg().toPlainString(), normal, true));
            res.addCell(cell(r.stoneCost().toPlainString(), normal, true));
            res.addCell(cell(msg(locale, "material.sand"), normal, false));
            res.addCell(cell(r.sandKg().toPlainString(), normal, true));
            res.addCell(cell(r.sandCost().toPlainString(), normal, true));
            PdfPCell totalLabel = cell(msg(locale, "calc.total"), section, false);
            totalLabel.setBackgroundColor(new Color(238, 243, 230));
            PdfPCell totalEmpty = cell("", normal, true);
            totalEmpty.setBackgroundColor(new Color(238, 243, 230));
            PdfPCell totalValue = cell(r.total().toPlainString(), section, true);
            totalValue.setBackgroundColor(new Color(238, 243, 230));
            res.addCell(totalLabel);
            res.addCell(totalEmpty);
            res.addCell(totalValue);
            doc.add(res);
        } catch (DocumentException | IOException e) {
            throw new IllegalStateException("Cannot create PDF", e);
        } finally {
            doc.close();
        }
        return out.toByteArray();
    }

    private String msg(Locale locale, String key) {
        return messages.getMessage(key, null, key, locale);
    }

    /** A heading with clear space above and below, so it never touches the table. */
    private static Paragraph sectionTitle(String text, Font font) {
        Paragraph p = new Paragraph(text, font);
        p.setSpacingBefore(18);
        p.setSpacingAfter(8);
        return p;
    }

    private static PdfPTable table(int columns, float[] widths, float spacingBefore, float spacingAfter) throws DocumentException {
        PdfPTable t = new PdfPTable(columns);
        t.setSpacingBefore(spacingBefore);
        t.setSpacingAfter(spacingAfter);
        t.setWidthPercentage(100);
        t.setWidths(widths);
        return t;
    }

    private static void row(PdfPTable t, Font font, String label, String value) {
        t.addCell(cell(label, font, false));
        t.addCell(cell(value, font, false));
    }

    private static void header(PdfPTable t, Font font, String... labels) {
        for (String label : labels) {
            PdfPCell c = cell(label, font, false);
            c.setBackgroundColor(new Color(230, 230, 230));
            t.addCell(c);
        }
    }

    private static PdfPCell cell(String text, Font font, boolean right) {
        PdfPCell c = new PdfPCell(new Phrase(text == null ? "" : text, font));
        c.setPadding(7);
        if (right) {
            c.setHorizontalAlignment(Element.ALIGN_RIGHT);
        }
        return c;
    }
}
