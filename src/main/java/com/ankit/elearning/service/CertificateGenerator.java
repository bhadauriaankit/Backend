package com.ankit.elearning.service;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.VerticalAlignment;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Generates a landscape PDF certificate of completion.
 * Uses iText 8 — add to pom.xml:
 *
 *   <dependency>
 *     <groupId>com.itextpdf</groupId>
 *     <artifactId>itext-core</artifactId>
 *     <version>8.0.4</version>
 *     <type>pom</type>
 *   </dependency>
 */
public class CertificateGenerator {

    // Brand colours
    private static final DeviceRgb INDIGO    = new DeviceRgb(79,  70, 229);
    private static final DeviceRgb VIOLET    = new DeviceRgb(124, 58, 237);
    private static final DeviceRgb GOLD      = new DeviceRgb(234,179,  8);
    private static final DeviceRgb DARK_BG   = new DeviceRgb( 15, 15, 26);
    private static final DeviceRgb LIGHT_TXT = new DeviceRgb(241,245,249);
    private static final DeviceRgb MUTED     = new DeviceRgb(100,116,139);
    private static final DeviceRgb VIOLET_LT = new DeviceRgb(196,181,253);

    public static byte[] generate(String studentName, String courseTitle, double percentage)
            throws Exception {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter   writer  = new PdfWriter(baos);
        PdfDocument pdf     = new PdfDocument(writer);
        PageSize    size    = PageSize.A4.rotate();  // landscape
        Document    doc     = new Document(pdf, size);
        doc.setMargins(0, 0, 0, 0);

        float W = size.getWidth();
        float H = size.getHeight();

        PdfCanvas canvas = new PdfCanvas(pdf.addNewPage());

        // ── Background ────────────────────────────────────────────────────────
        canvas.setFillColor(DARK_BG).rectangle(0, 0, W, H).fill();

        // ── Gradient border strips (top + bottom) ─────────────────────────────
        float stripH = 8f;
        // draw 6 equal segments for gradient effect
        float segW = W / 6f;
        DeviceRgb[] grad = {
                new DeviceRgb(0, 212, 255),
                new DeviceRgb(41, 196, 242),
                new DeviceRgb(106,159,214),
                new DeviceRgb(168,114,184),
                new DeviceRgb(212, 74,138),
                new DeviceRgb(233, 30, 99)
        };
        for (int i = 0; i < 6; i++) {
            canvas.setFillColor(grad[i])
                    .rectangle(i * segW, H - stripH, segW, stripH).fill()
                    .rectangle(i * segW, 0,          segW, stripH).fill();
        }

        // ── Decorative corner circles ─────────────────────────────────────────
        float r = 80f;
        canvas.setFillColor(new DeviceRgb(30,27,75)).setStrokeColor(INDIGO);
        for (float cx : new float[]{0, W}) {
            for (float cy : new float[]{0, H}) {
                canvas.circle(cx, cy, r).fillStroke();
            }
        }

        // ── Star / seal decoration ────────────────────────────────────────────
        canvas.setFillColor(GOLD).circle(W / 2f, H - 70, 28).fill();
        canvas.setFillColor(DARK_BG).circle(W / 2f, H - 70, 20).fill();
        canvas.setFillColor(GOLD).circle(W / 2f, H - 70, 10).fill();

        // ── Fonts (Helvetica is built-in, no external file needed) ───────────
        PdfFont bold   = PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA_BOLD);
        PdfFont normal = PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA);

        // ── Layout (all text via Document / Paragraph) ────────────────────────
        float top = H - stripH;

        // "CERTIFICATE OF COMPLETION"
        doc.add(new Paragraph("CERTIFICATE OF COMPLETION")
                .setFont(bold).setFontSize(11).setFontColor(VIOLET_LT)
                .setCharacterSpacing(4f)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(top - H + 100)   // 100px from top strip
                .setMarginBottom(4));

        // "EduLearn" brand
        doc.add(new Paragraph("◈ EduLearn")
                .setFont(bold).setFontSize(22).setFontColor(INDIGO)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20));

        // Divider line
        canvas.setStrokeColor(INDIGO).setLineWidth(0.5f)
                .moveTo(W * 0.2f, H - 160).lineTo(W * 0.8f, H - 160).stroke();

        // "This certifies that"
        doc.add(new Paragraph("This certifies that")
                .setFont(normal).setFontSize(13).setFontColor(MUTED)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(14).setMarginBottom(8));

        // Student name
        doc.add(new Paragraph(studentName)
                .setFont(bold).setFontSize(38).setFontColor(LIGHT_TXT)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(10));

        // "has successfully completed"
        doc.add(new Paragraph("has successfully completed the course")
                .setFont(normal).setFontSize(13).setFontColor(MUTED)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(10));

        // Course title
        doc.add(new Paragraph(courseTitle)
                .setFont(bold).setFontSize(24).setFontColor(GOLD)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(10));

        // Score
        doc.add(new Paragraph(String.format("with a score of %.1f%%", percentage))
                .setFont(normal).setFontSize(13).setFontColor(VIOLET_LT)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(30));

        // Divider
        canvas.setStrokeColor(INDIGO).setLineWidth(0.5f)
                .moveTo(W * 0.2f, 140).lineTo(W * 0.8f, 140).stroke();

        // Date + platform name at bottom
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy"));
        doc.add(new Paragraph("Issued on " + date + "  |  EduLearn Platform")
                .setFont(normal).setFontSize(11).setFontColor(MUTED)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(10));

        doc.close();
        return baos.toByteArray();
    }
}