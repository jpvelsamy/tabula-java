# Session Notes - Al Mazaya PDF Table Detection

## Objective
Create JUnit test cases to identify which pages in the Al Mazaya 2024 PDF contain tables using tabula-java's built-in detection algorithms.

## PDF Document
- **File**: `src/test/resources/technology/tabula/Al Mazaya - 2024 .pdf`
- **Total Pages**: 61
- **Content**: Al-Mazaya Holding Company's 2024 consolidated financial statements

## Test File Created
- **Location**: `src/test/java/technology/tabula/TestAlMazayaTableDetection.java`

### Test Methods
1. `testNurminenDetectionAlgorithm()` - Uses text edge detection (comprehensive, works with/without ruling lines)
2. `testSpreadsheetDetectionAlgorithm()` - Uses ruling lines detection (for bordered tables)
3. `testCompareDetectionAlgorithms()` - Compares both algorithms side by side
4. `testListAllPagesWithTableStatus()` - Complete page-by-page analysis

## Detection Results

### Pages WITH Tables Detected

| Algorithm | Page Count | Pages |
|-----------|------------|-------|
| Nurminen | 23 | 8, 9, 10, 11, 12, 16, 19, 27, 40, 41, 43, 44, 45, 46, 48, 50, 51, 53, 56, 57, 58, 59, 60 |
| Spreadsheet | 2 | 1, 9 |

### Algorithm Comparison
- **Both algorithms detected**: Page 9
- **Only Nurminen detected**: 22 pages (8, 10, 11, 12, 16, 19, 27, 40, 41, 43, 44, 45, 46, 48, 50, 51, 53, 56, 57, 58, 59, 60)
- **Only Spreadsheet detected**: Page 1

### Pages WITHOUT Tables (37 pages)
2, 3, 4, 5, 6, 7, 13, 14, 15, 17, 18, 20, 21, 22, 23, 24, 25, 26, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 42, 47, 49, 52, 54, 55, 61

## Page 7 Investigation - False Negative Analysis

### Problem
Page 7 contains a table (Consolidated Statement of Financial Position) but was NOT detected by either algorithm.

### Root Cause
**Page 7 is a scanned image, not native PDF text.**

### Evidence
```
Page 7:  2 images, 0 chars of text  ← SCANNED IMAGE
Page 8:  0 images, 2968 chars of text  ✓ Native PDF
Page 9:  0 images, 1648 chars of text  ✓ Native PDF
Page 10: 0 images, 3288 chars of text  ✓ Native PDF
```

### Image Details on Page 7
| Image | Dimensions | Color Space | Description |
|-------|------------|-------------|-------------|
| Obj4 | 1240x1754 px | DeviceRGB | Color image |
| Obj5 | 1928x3028 px | DeviceGray | Main scanned page (1-bit) |

### Why Tabula Didn't Detect It
Tabula extracts **text elements** and **ruling lines** from native PDF content. Page 7 contains:
- **0 text elements** - Content is pixels, not text glyphs
- **0 rulings** - Lines are part of the raster image

The table exists visually but is embedded as a **raster image**, not as extractable PDF objects.

### Tabula Limitation
This is a fundamental limitation of tabula-java - it cannot process scanned/image-based PDFs without OCR preprocessing.

### Solution Options
1. **OCR the PDF first** - Use tools like Tesseract, Adobe Acrobat, or `ocrmypdf` to convert scanned images to searchable text
2. **Re-export from source** - If you have access to the original document, export as native PDF (not scanned/printed)
3. **Use image-based table extraction** - Libraries like `camelot` (Python) with `flavor='lattice'` can handle image-based tables

### Investigation Test Files Created
- `src/test/java/technology/tabula/TestPage7Investigation.java` - Analyzes page content
- `src/test/java/technology/tabula/TestPage7ImageCheck.java` - Checks for embedded images

## Detection Algorithms Used

### NurminenDetectionAlgorithm
- Location: `src/main/java/technology/tabula/detectors/NurminenDetectionAlgorithm.java`
- Based on Anssi Nurminen's master's thesis
- Uses image-based edge detection and text alignment analysis
- More comprehensive - detects tables with and without ruling lines

### SpreadsheetDetectionAlgorithm
- Location: `src/main/java/technology/tabula/detectors/SpreadsheetDetectionAlgorithm.java`
- Uses intersecting ruling lines to find tables
- Works best for tables with visible grid lines/borders

## pom.xml Changes Made

### Issue Resolved
```
Project build error: Unresolveable build extension:
Plugin org.sonatype.plugins:nexus-staging-maven-plugin:1.7.0
```

### Fix Applied
Moved `nexus-staging-maven-plugin` from main `<build>` section to `<profiles><profile id="release">` section.

This plugin is only needed for publishing to Maven Central, not for local development.

## Eclipse Setup Instructions

### Import Project
1. File → Import → Maven → Existing Maven Projects
2. Browse to `/home/jpvel/Workspace/tabula-java`
3. Click Finish

### Refresh Dependencies
1. Right-click project → Maven → Update Project... (Alt+F5)
2. Check "Force Update of Snapshots/Releases"
3. Click OK

### Run JUnit Tests
1. Navigate to `src/test/java/technology/tabula/TestAlMazayaTableDetection.java`
2. Right-click → Run As → JUnit Test
3. View results in JUnit view and Console view

## Run Tests from Command Line

```bash
# Run all tests in the class
mvn test -Dtest=TestAlMazayaTableDetection

# Run specific test method
mvn test -Dtest=TestAlMazayaTableDetection#testListAllPagesWithTableStatus

# Run with quiet output
mvn test -Dtest=TestAlMazayaTableDetection -q
```

## Key Classes Reference

| Class | Location | Purpose |
|-------|----------|---------|
| DetectionAlgorithm | `src/main/java/technology/tabula/detectors/DetectionAlgorithm.java` | Interface for detection algorithms |
| NurminenDetectionAlgorithm | `src/main/java/technology/tabula/detectors/NurminenDetectionAlgorithm.java` | Text/image-based detection |
| SpreadsheetDetectionAlgorithm | `src/main/java/technology/tabula/detectors/SpreadsheetDetectionAlgorithm.java` | Ruling-line based detection |
| ObjectExtractor | `src/main/java/technology/tabula/ObjectExtractor.java` | Extracts Page objects from PDF |
| Page | `src/main/java/technology/tabula/Page.java` | Contains extracted content from a PDF page |
| Rectangle | `src/main/java/technology/tabula/Rectangle.java` | Represents detected table areas |

## Dependencies (from pom.xml)

- JUnit 4.13.2 (test scope)
- PDFBox 3.0.4
- Gson 2.11.0
- Commons CSV 1.11.0

## Session Date
2026-01-18
