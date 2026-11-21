package org.geocyber.presentation;

import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class PresentationGeneratorTest {

    @Test
    @DisplayName("Should generate executive 15-slide PowerPoint deck with Apache POI")
    void testGenerate15SlidePresentation() throws IOException {
        PresentationGenerator generator = new PresentationGenerator();
        String targetPath = "C:/Users/venna/.gemini/antigravity/scratch/geocyber-intel/GEOCYBER_INTEL_Presentation.pptx";

        File file = generator.generatePresentation(targetPath);
        assertNotNull(file);
        assertTrue(file.exists());
        assertTrue(file.length() > 10000, "Presentation file size should exceed 10 KB");

        try (FileInputStream fis = new FileInputStream(file);
             XMLSlideShow ppt = new XMLSlideShow(fis)) {
            assertEquals(15, ppt.getSlides().size(), "Presentation must contain exactly 15 master slides");
        }
    }
}
