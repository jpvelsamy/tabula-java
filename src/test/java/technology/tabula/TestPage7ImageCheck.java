package technology.tabula;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.Test;

import java.io.File;

/**
 * Check if page 7 contains images instead of text.
 */
public class TestPage7ImageCheck {

    private static final String PDF_PATH = "src/test/resources/technology/tabula/Al Mazaya - 2024 .pdf";

    @Test
    public void checkPage7ForImages() throws Exception {
        File pdfFile = new File(PDF_PATH);

        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            System.out.println("=== Page 7 Image Analysis ===\n");

            // Get page 7 (0-indexed = page 6)
            PDPage page = document.getPage(6);

            // Check for images in resources
            PDResources resources = page.getResources();
            System.out.println("1. CHECKING FOR IMAGES ON PAGE 7:");

            int imageCount = 0;
            for (COSName name : resources.getXObjectNames()) {
                PDXObject xobject = resources.getXObject(name);
                if (xobject instanceof PDImageXObject) {
                    imageCount++;
                    PDImageXObject image = (PDImageXObject) xobject;
                    System.out.println("   Found image: " + name.getName());
                    System.out.println("     Width: " + image.getWidth() + " px");
                    System.out.println("     Height: " + image.getHeight() + " px");
                    System.out.println("     Color space: " + image.getColorSpace().getName());
                    System.out.println("     Bits per component: " + image.getBitsPerComponent());
                }
            }

            if (imageCount == 0) {
                System.out.println("   No images found on page 7");
            } else {
                System.out.println("\n   Total images on page 7: " + imageCount);
            }

            // Try PDFTextStripper to get raw text
            System.out.println("\n2. RAW TEXT EXTRACTION (PDFTextStripper):");
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setStartPage(7);
            stripper.setEndPage(7);
            String text = stripper.getText(document);

            if (text.trim().isEmpty()) {
                System.out.println("   No text extracted from page 7");
            } else {
                System.out.println("   Text found (first 500 chars):");
                String preview = text.length() > 500 ? text.substring(0, 500) + "..." : text;
                System.out.println(preview);
            }

            // Compare with other pages
            System.out.println("\n3. COMPARISON WITH OTHER PAGES:");
            for (int i = 6; i <= 9; i++) {
                PDPage p = document.getPage(i);
                PDResources res = p.getResources();
                int imgs = 0;
                for (COSName name : res.getXObjectNames()) {
                    if (res.getXObject(name) instanceof PDImageXObject) {
                        imgs++;
                    }
                }

                stripper.setStartPage(i + 1);
                stripper.setEndPage(i + 1);
                String pageText = stripper.getText(document);
                int textLen = pageText.trim().length();

                System.out.println("   Page " + (i + 1) + ": " + imgs + " images, " + textLen + " chars of text");
            }
        }
    }
}
