package technology.tabula;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import technology.tabula.detectors.DetectionAlgorithm;
import technology.tabula.detectors.NurminenDetectionAlgorithm;
import technology.tabula.detectors.SpreadsheetDetectionAlgorithm;

import java.io.File;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.*;

/**
 * Test class for detecting tables in the Al Mazaya 2024 PDF document.
 * Uses tabula's built-in detection algorithms to identify which pages contain tables.
 */
public class TestAlMazayaTableDetection {

    private static final String PDF_PATH = "src/test/resources/technology/tabula/Al Mazaya - 2024 .pdf";

    private static Level defaultLogLevel;

    @BeforeClass
    public static void disableLogging() {
        Logger pdfboxLogger = Logger.getLogger("org.apache.pdfbox");
        defaultLogLevel = pdfboxLogger.getLevel();
        pdfboxLogger.setLevel(Level.OFF);
    }

    @AfterClass
    public static void enableLogging() {
        Logger.getLogger("org.apache.pdfbox").setLevel(defaultLogLevel);
    }

    /**
     * Detects tables using the NurminenDetectionAlgorithm.
     * This algorithm uses image-based edge detection and text alignment analysis.
     * It is more comprehensive and can detect tables with and without ruling lines.
     */
    @Test
    public void testNurminenDetectionAlgorithm() throws Exception {
        File pdfFile = new File(PDF_PATH);
        assertTrue("PDF file should exist: " + PDF_PATH, pdfFile.exists());

        DetectionAlgorithm detector = new NurminenDetectionAlgorithm();
        Map<Integer, List<Rectangle>> pagesWithTables = new TreeMap<>();

        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            ObjectExtractor extractor = new ObjectExtractor(document);
            PageIterator pages = extractor.extract();

            System.out.println("=== Nurminen Detection Algorithm Results ===");
            System.out.println("Analyzing " + document.getNumberOfPages() + " pages...\n");

            while (pages.hasNext()) {
                Page page = pages.next();
                int pageNumber = page.getPageNumber();

                List<Rectangle> detectedTables = detector.detect(page);

                if (!detectedTables.isEmpty()) {
                    pagesWithTables.put(pageNumber, detectedTables);
                    System.out.println("Page " + pageNumber + ": " + detectedTables.size() + " table(s) detected");
                    for (int i = 0; i < detectedTables.size(); i++) {
                        Rectangle table = detectedTables.get(i);
                        System.out.println("  Table " + (i + 1) + ": " + formatRectangle(table));
                    }
                }
            }
        }

        System.out.println("\n=== Summary (Nurminen Algorithm) ===");
        System.out.println("Total pages with tables: " + pagesWithTables.size());
        System.out.println("Pages: " + pagesWithTables.keySet());

        // Ensure we detected at least some tables (this is a financial document with tables)
        assertFalse("Should detect tables in financial document", pagesWithTables.isEmpty());
    }

    /**
     * Detects tables using the SpreadsheetDetectionAlgorithm.
     * This algorithm uses intersecting ruling lines to find tables.
     * It works best for tables with visible grid lines/borders.
     */
    @Test
    public void testSpreadsheetDetectionAlgorithm() throws Exception {
        File pdfFile = new File(PDF_PATH);
        assertTrue("PDF file should exist: " + PDF_PATH, pdfFile.exists());

        DetectionAlgorithm detector = new SpreadsheetDetectionAlgorithm();
        Map<Integer, List<Rectangle>> pagesWithTables = new TreeMap<>();

        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            ObjectExtractor extractor = new ObjectExtractor(document);
            PageIterator pages = extractor.extract();

            System.out.println("=== Spreadsheet Detection Algorithm Results ===");
            System.out.println("Analyzing " + document.getNumberOfPages() + " pages...\n");

            while (pages.hasNext()) {
                Page page = pages.next();
                int pageNumber = page.getPageNumber();

                List<Rectangle> detectedTables = detector.detect(page);

                if (!detectedTables.isEmpty()) {
                    pagesWithTables.put(pageNumber, detectedTables);
                    System.out.println("Page " + pageNumber + ": " + detectedTables.size() + " table(s) detected");
                    for (int i = 0; i < detectedTables.size(); i++) {
                        Rectangle table = detectedTables.get(i);
                        System.out.println("  Table " + (i + 1) + ": " + formatRectangle(table));
                    }
                }
            }
        }

        System.out.println("\n=== Summary (Spreadsheet Algorithm) ===");
        System.out.println("Total pages with tables: " + pagesWithTables.size());
        System.out.println("Pages: " + pagesWithTables.keySet());
    }

    /**
     * Compares results from both detection algorithms.
     * Provides a comprehensive view of which pages have tables according to each algorithm.
     */
    @Test
    public void testCompareDetectionAlgorithms() throws Exception {
        File pdfFile = new File(PDF_PATH);
        assertTrue("PDF file should exist: " + PDF_PATH, pdfFile.exists());

        DetectionAlgorithm nurminenDetector = new NurminenDetectionAlgorithm();
        DetectionAlgorithm spreadsheetDetector = new SpreadsheetDetectionAlgorithm();

        Map<Integer, Integer> nurminenResults = new TreeMap<>();
        Map<Integer, Integer> spreadsheetResults = new TreeMap<>();

        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            ObjectExtractor extractor = new ObjectExtractor(document);
            int totalPages = document.getNumberOfPages();

            System.out.println("=== Comparison of Detection Algorithms ===");
            System.out.println("Total pages in document: " + totalPages + "\n");
            System.out.println(String.format("%-6s | %-20s | %-20s", "Page", "Nurminen Tables", "Spreadsheet Tables"));
            System.out.println("-".repeat(52));

            for (int pageNum = 1; pageNum <= totalPages; pageNum++) {
                Page page = extractor.extract(pageNum);

                List<Rectangle> nurminenTables = nurminenDetector.detect(page);
                List<Rectangle> spreadsheetTables = spreadsheetDetector.detect(page);

                if (!nurminenTables.isEmpty()) {
                    nurminenResults.put(pageNum, nurminenTables.size());
                }
                if (!spreadsheetTables.isEmpty()) {
                    spreadsheetResults.put(pageNum, spreadsheetTables.size());
                }

                if (!nurminenTables.isEmpty() || !spreadsheetTables.isEmpty()) {
                    System.out.println(String.format("%-6d | %-20d | %-20d",
                            pageNum,
                            nurminenTables.size(),
                            spreadsheetTables.size()));
                }
            }
        }

        System.out.println("\n=== Summary ===");
        System.out.println("Pages with tables (Nurminen): " + nurminenResults.size() + " - " + nurminenResults.keySet());
        System.out.println("Pages with tables (Spreadsheet): " + spreadsheetResults.size() + " - " + spreadsheetResults.keySet());

        // Find pages detected by both algorithms
        Set<Integer> bothDetected = new TreeSet<>(nurminenResults.keySet());
        bothDetected.retainAll(spreadsheetResults.keySet());
        System.out.println("Pages detected by both: " + bothDetected.size() + " - " + bothDetected);

        // Find pages detected only by Nurminen
        Set<Integer> onlyNurminen = new TreeSet<>(nurminenResults.keySet());
        onlyNurminen.removeAll(spreadsheetResults.keySet());
        System.out.println("Pages detected only by Nurminen: " + onlyNurminen.size() + " - " + onlyNurminen);

        // Find pages detected only by Spreadsheet
        Set<Integer> onlySpreadsheet = new TreeSet<>(spreadsheetResults.keySet());
        onlySpreadsheet.removeAll(nurminenResults.keySet());
        System.out.println("Pages detected only by Spreadsheet: " + onlySpreadsheet.size() + " - " + onlySpreadsheet);
    }

    /**
     * Lists all pages and their table detection status using both algorithms.
     * Useful for getting a complete overview of the document.
     */
    @Test
    public void testListAllPagesWithTableStatus() throws Exception {
        File pdfFile = new File(PDF_PATH);
        assertTrue("PDF file should exist: " + PDF_PATH, pdfFile.exists());

        DetectionAlgorithm nurminenDetector = new NurminenDetectionAlgorithm();
        DetectionAlgorithm spreadsheetDetector = new SpreadsheetDetectionAlgorithm();

        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            ObjectExtractor extractor = new ObjectExtractor(document);
            int totalPages = document.getNumberOfPages();

            System.out.println("=== Complete Page-by-Page Analysis ===");
            System.out.println("Document: " + PDF_PATH);
            System.out.println("Total pages: " + totalPages + "\n");

            List<Integer> pagesWithNurminenTables = new ArrayList<>();
            List<Integer> pagesWithSpreadsheetTables = new ArrayList<>();
            List<Integer> pagesWithNoTables = new ArrayList<>();

            for (int pageNum = 1; pageNum <= totalPages; pageNum++) {
                Page page = extractor.extract(pageNum);

                List<Rectangle> nurminenTables = nurminenDetector.detect(page);
                List<Rectangle> spreadsheetTables = spreadsheetDetector.detect(page);

                boolean hasNurminen = !nurminenTables.isEmpty();
                boolean hasSpreadsheet = !spreadsheetTables.isEmpty();

                if (hasNurminen) {
                    pagesWithNurminenTables.add(pageNum);
                }
                if (hasSpreadsheet) {
                    pagesWithSpreadsheetTables.add(pageNum);
                }
                if (!hasNurminen && !hasSpreadsheet) {
                    pagesWithNoTables.add(pageNum);
                }

                String status = hasNurminen || hasSpreadsheet ? "HAS TABLES" : "NO TABLES";
                String details = String.format("(Nurminen: %d, Spreadsheet: %d)",
                        nurminenTables.size(), spreadsheetTables.size());

                System.out.println(String.format("Page %2d: %-12s %s", pageNum, status, details));
            }

            System.out.println("\n=== Final Summary ===");
            System.out.println("Pages WITH tables (Nurminen): " + pagesWithNurminenTables);
            System.out.println("Pages WITH tables (Spreadsheet): " + pagesWithSpreadsheetTables);
            System.out.println("Pages WITHOUT tables (both algorithms): " + pagesWithNoTables);
        }
    }

    private String formatRectangle(Rectangle rect) {
        return String.format("top=%.1f, left=%.1f, width=%.1f, height=%.1f",
                rect.getTop(), rect.getLeft(), rect.getWidth(), rect.getHeight());
    }
}
