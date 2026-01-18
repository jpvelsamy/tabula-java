# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

tabula-java is a Java library for extracting tables from PDF files. It powers the Tabula web application and can be used as a command-line tool or library.

## Build Commands

```bash
# Build executable JAR with dependencies
mvn clean compile assembly:single

# Run all tests
mvn test

# Run single test class
mvn test -Dtest=TestBasicExtractor

# Run single test method
mvn test -Dtest=TestBasicExtractor#testExtraction

# Generate Javadoc
mvn javadoc:javadoc
```

The built JAR is created at `target/tabula-{version}-jar-with-dependencies.jar`.

## Architecture

### Extraction Pipeline

```
PDF File → ObjectExtractor → Page (TextElements + Rulings) → ExtractionAlgorithm → Table → Writer
```

1. **ObjectExtractor** parses PDFs using PDFBox, producing `Page` objects containing `TextElement` and `Ruling` objects
2. **ExtractionAlgorithm** processes pages to extract tables:
   - `BasicExtractionAlgorithm` (stream mode) - text/column-based, for tables without ruling lines
   - `SpreadsheetExtractionAlgorithm` (lattice mode) - ruling-based, for tables with grid lines
3. **Writer** outputs tables as CSV, TSV, or JSON

### Key Classes

- `Rectangle` - Base geometry class extending `Rectangle2D.Float`, used by all spatial objects
- `TextElement` - Individual character/glyph from PDF with position and font info
- `Ruling` - Horizontal/vertical lines extending `Line2D.Float`, used for table borders
- `Page` - Contains all extracted content from a PDF page; uses `RectangleSpatialIndex` for efficient text lookup
- `Table` - Result container with cells indexed by `(row, col)` position
- `CommandLineApp` - CLI entry point at `technology.tabula.CommandLineApp`

### Interfaces

- `ExtractionAlgorithm` - implement to add new extraction strategies
- `DetectionAlgorithm` - implement to add new table detection methods (see `NurminenDetectionAlgorithm`)
- `Writer` - implement to add new output formats

## CLI Usage

```bash
java -jar target/tabula-{version}-jar-with-dependencies.jar [options] <pdf>

# Key options:
# -l/--lattice    Use spreadsheet extraction (ruling-based)
# -t/--stream     Use basic extraction (text-based)
# -g/--guess      Auto-detect table areas
# -a/--area       Restrict to rectangular region (y1,x1,y2,x2 in points)
# -p/--pages      Page ranges (e.g., 1-3,5-7 or all)
# -f/--format     Output format (CSV/TSV/JSON)
```

## Library Usage

```java
try (PDDocument document = PDDocument.load(new File("my.pdf"))) {
    SpreadsheetExtractionAlgorithm sea = new SpreadsheetExtractionAlgorithm();
    PageIterator pi = new ObjectExtractor(document).extract();
    while (pi.hasNext()) {
        Page page = pi.next();
        List<Table> tables = sea.extract(page);
        for (Table table : tables) {
            List<List<RectangularTextContainer>> rows = table.getRows();
            // process rows...
        }
    }
}
```

## Technical Notes

- PDF coordinate system: origin at top-left, Y increases downward
- Uses floating-point tolerance comparisons for geometry (see `Utils.within()`, `Utils.feq()`)
- `Page.getArea(Rectangle)` crops content to a specific region
- Tests run with `-Xms1024m -Xmx2048m` heap settings
- Test PDFs are in `src/test/resources/technology/tabula/`
