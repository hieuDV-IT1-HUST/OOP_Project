package utils;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

public class OCRUtils {
    private static final Logger logger = LogManager.getLogger(OCRUtils.class);
    public static String recognizeTextFromImage(File imageFile) {
        ITesseract tesseract = new Tesseract();
        tesseract.setDatapath("tessdata");
        tesseract.setLanguage("eng");

        try {
            return tesseract.doOCR(imageFile).trim();
        } catch (TesseractException e) {
            logger.error(e);
            return null;
        }
    }
}