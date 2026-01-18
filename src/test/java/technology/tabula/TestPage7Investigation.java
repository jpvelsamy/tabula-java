package technology.tabula;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.Test;
import technology.tabula.detectors.NurminenDetectionAlgorithm;
import technology.tabula.detectors.SpreadsheetDetectionAlgorithm;
import technology.tabula.extractors.BasicExtractionAlgorithm;
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm;

import java.io.File;
import java.util.List;

/**
 * Investigation test for page 7 of Al Mazaya 2024 PDF.
 * Analyzes why table detection algorithms may have missed a table.
 */
public class TestPage7Investigation {

    private static final String PDF_PATH = "src/test/resources/technology/tabula/Al Mazaya - 2024 .pdf";
    private static final int PAGE_NUMBER = 7;

    @Test
    public void investigatePage7() throws Exception {
        File pdfFile = new File(PDF_PATH);

        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            ObjectExtractor extractor = new ObjectExtractor(document);
            Page page = extractor.extract(PAGE_NUMBER);

            System.out.println("=== Page 7 Investigation ===\n");

            // 1. Basic page info
            System.out.println("1. PAGE DIMENSIONS:");
            System.out.println("   Width: " + page.getWidth());
            System.out.println("   Height: " + page.getHeight());
            System.out.println();

            // 2. Text elements
            List<TextElement> textElements = page.getText();
            System.out.println("2. TEXT ELEMENTS:");
            System.out.println("   Total text elements: " + textElements.size());
            if (!textElements.isEmpty()) {
                System.out.println("   Sample text (first 20 elements):");
                for (int i = 0; i < Math.min(20, textElements.size()); i++) {
                    TextElement te = textElements.get(i);
                    System.out.println("     [" + i + "] '" + te.getText() + "' at (" +
                        String.format("%.1f", te.getLeft()) + ", " +
                        String.format("%.1f", te.getTop()) + ")");
                }
            }
            System.out.println();

            // 3. Rulings (lines)
            List<Ruling> horizontalRulings = page.getHorizontalRulings();
            List<Ruling> verticalRulings = page.getVerticalRulings();
            System.out.println("3. RULINGS (LINES):");
            System.out.println("   Horizontal rulings: " + horizontalRulings.size());
            System.out.println("   Vertical rulings: " + verticalRulings.size());

            if (!horizontalRulings.isEmpty()) {
                System.out.println("   Horizontal ruling positions:");
                for (int i = 0; i < Math.min(10, horizontalRulings.size()); i++) {
                    Ruling r = horizontalRulings.get(i);
                    System.out.println("     Y=" + String.format("%.1f", r.getY1()) +
                        " from X=" + String.format("%.1f", r.getX1()) +
                        " to X=" + String.format("%.1f", r.getX2()));
                }
            }

            if (!verticalRulings.isEmpty()) {
                System.out.println("   Vertical ruling positions:");
                for (int i = 0; i < Math.min(10, verticalRulings.size()); i++) {
                    Ruling r = verticalRulings.get(i);
                    System.out.println("     X=" + String.format("%.1f", r.getX1()) +
                        " from Y=" + String.format("%.1f", r.getY1()) +
                        " to Y=" + String.format("%.1f", r.getY2()));
                }
            }
            System.out.println();

            // 4. Detection algorithm results
            System.out.println("4. DETECTION ALGORITHM RESULTS:");

            NurminenDetectionAlgorithm nurminen = new NurminenDetectionAlgorithm();
            List<Rectangle> nurminenTables = nurminen.detect(page);
            System.out.println("   Nurminen detected: " + nurminenTables.size() + " tables");
            for (Rectangle r : nurminenTables) {
                System.out.println("     " + formatRectangle(r));
            }

            SpreadsheetDetectionAlgorithm spreadsheet = new SpreadsheetDetectionAlgorithm();
            List<Rectangle> spreadsheetTables = spreadsheet.detect(page);
            System.out.println("   Spreadsheet detected: " + spreadsheetTables.size() + " tables");
            for (Rectangle r : spreadsheetTables) {
                System.out.println("     " + formatRectangle(r));
            }
            System.out.println();

            // 5. Try extraction algorithms directly
            System.out.println("5. EXTRACTION ALGORITHM RESULTS:");

            BasicExtractionAlgorithm basic = new BasicExtractionAlgorithm();
            List<Table> basicTables = basic.extract(page);
            System.out.println("   Basic extraction found: " + basicTables.size() + " tables");
            for (int i = 0; i < basicTables.size(); i++) {
                Table t = basicTables.get(i);
                System.out.println("     Table " + (i+1) + ": " + t.getRowCount() + " rows, " + t.getColCount() + " cols");
            }

            SpreadsheetExtractionAlgorithm sea = new SpreadsheetExtractionAlgorithm();
            List<Table> spreadsheetExtractedTables = sea.extract(page);
            System.out.println("   Spreadsheet extraction found: " + spreadsheetExtractedTables.size() + " tables");
            for (int i = 0; i < spreadsheetExtractedTables.size(); i++) {
                Table t = spreadsheetExtractedTables.get(i);
                System.out.println("     Table " + (i+1) + ": " + t.getRowCount() + " rows, " + t.getColCount() + " cols");
            }
            System.out.println();

            // 6. Text chunks and lines analysis
            System.out.println("6. TEXT STRUCTURE ANALYSIS:");
            List<TextChunk> textChunks = TextElement.mergeWords(page.getText());
            System.out.println("   Text chunks (words): " + textChunks.size());

            List<Line> lines = TextChunk.groupByLines(textChunks);
            System.out.println("   Text lines: " + lines.size());

            System.out.println("\n   Line details (showing all lines):");
            for (int i = 0; i < lines.size(); i++) {
                Line line = lines.get(i);
                StringBuilder lineText = new StringBuilder();
                for (TextChunk chunk : line.getTextElements()) {
                    lineText.append(chunk.getText()).append(" | ");
                }
                String text = lineText.toString();
                if (text.length() > 100) {
                    text = text.substring(0, 100) + "...";
                }
                System.out.println("     Line " + (i+1) + " (Y=" + String.format("%.1f", line.getTop()) +
                    ", chunks=" + line.getTextElements().size() + "): " + text);
            }
        }
    }

    private String formatRectangle(Rectangle rect) {
        return String.format("top=%.1f, left=%.1f, width=%.1f, height=%.1f, bottom=%.1f, right=%.1f",
                rect.getTop(), rect.getLeft(), rect.getWidth(), rect.getHeight(),
                rect.getBottom(), rect.getRight());
    }
}
