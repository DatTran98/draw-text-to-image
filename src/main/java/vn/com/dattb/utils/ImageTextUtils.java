package vn.com.dattb.utils;

import com.itextpdf.forms.PdfAcroForm;
import com.itextpdf.forms.fields.PdfSignatureFormField;
import com.itextpdf.forms.fields.PdfTextFormField;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.element.Image;
import com.itextpdf.signatures.PdfSignatureAppearance;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static java.util.Objects.nonNull;

/***
 * This class provides utility methods to add text information to an image
 * @author DatTran98 (dattb.com)
 * @version 1.0.0
 * @since 2024-11-22
 */
public class ImageTextUtils {

    // Prevent instantiation
    private ImageTextUtils() {
        throw new IllegalStateException("Utility class");
    }

    /***
     * This method to add text information to a Base64 image
     * @param base64Image Base64 image
     * @param infoMap Information map with key-value pairs(key: label, value: text)
     * @return Base64 image with text information
     * @throws IOException If an error occurs during image processing
     */
    public static String addInfoToBase64Image(String base64Image, Map<String, String> infoMap) throws IOException {
        // Decode Base64 to BufferedImage
        byte[] imageBytes = Base64.getDecoder().decode(base64Image);
        BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageBytes));

        int imageWidth = originalImage.getWidth();
        int imageHeight = originalImage.getHeight();

        // Default padding and line spacing
        int padding = 20;

        // Create a temporary image for font calculations
        BufferedImage tempImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D tempG2D = tempImage.createGraphics();
        Font font = new Font("Arial", Font.PLAIN, 12); // Start with a base font
        tempG2D.setFont(font);

        // Calculate the maximum font size to fit text in the image width
        int maxTextWidth = imageWidth - (2 * padding); // Subtract left and right padding
        FontMetrics metrics = tempG2D.getFontMetrics();
        for (Map.Entry<String, String> entry : infoMap.entrySet()) {
            String text = entry.getKey() + ": " + entry.getValue();
            while (metrics.stringWidth(text) < maxTextWidth) {
                font = font.deriveFont((float) font.getSize() + 1);
                tempG2D.setFont(font);
                metrics = tempG2D.getFontMetrics();
            }
            if (metrics.stringWidth(text) > maxTextWidth) {
                font = font.deriveFont((float) font.getSize() - 1); // Step back
                tempG2D.setFont(font);
                metrics = tempG2D.getFontMetrics();
            }
        }
        tempG2D.dispose();

        // Calculate line height and additional height for wrapped text
        int lineHeight = metrics.getAscent() + metrics.getDescent() + metrics.getLeading();
        int additionalHeight = 0;

        // Calculate the total height needed for wrapped text
        for (Map.Entry<String, String> entry : infoMap.entrySet()) {
            String text = entry.getKey() + ": " + entry.getValue();
            additionalHeight += wrapTextHeight(metrics, text, maxTextWidth, lineHeight);
        }
        additionalHeight += 2 * padding; // Top and bottom padding

        // Create a new image with increased height
        int newHeight = imageHeight + additionalHeight;
        BufferedImage newImage = new BufferedImage(imageWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = newImage.createGraphics();

        // Draw the original image, that was decoded from Base64
        g2d.drawImage(originalImage, 0, 0, null);

        // Set text rendering properties
        g2d.setColor(Color.BLACK);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setFont(font);

        // Now draw wrapped text below the image
        int yPosition = imageHeight + padding + metrics.getAscent();
        for (Map.Entry<String, String> entry : infoMap.entrySet()) {
            String text = entry.getKey() + ": " + entry.getValue();
            yPosition = drawWrappedText(g2d, metrics, text, padding, yPosition, maxTextWidth, lineHeight);
        }

        g2d.dispose();

        // Encode the updated image back to Base64
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(newImage, "png", baos);
        byte[] updatedImageBytes = baos.toByteArray();
        return Base64.getEncoder().encodeToString(updatedImageBytes);
    }

    /***
     * This method to calculate the height of wrapped text
     * @param metrics FontMetrics object
     * @param text Text to wrap
     * @param maxWidth Maximum width for wrapped text
     * @param lineHeight Line height
     * @return Height of wrapped text
     */
    private static int wrapTextHeight(FontMetrics metrics, String text, int maxWidth, int lineHeight) {
        int lines = 0;
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();

        for (String word : words) {
            if (metrics.stringWidth(line + word) <= maxWidth) {
                line.append(word).append(" ");
            } else {
                lines++;
                line = new StringBuilder(word).append(" ");
            }
        }

        if (!line.isEmpty()) {
            lines++;
        }

        return lines * lineHeight;
    }

    /***
     * This method to draw wrapped text on the image
     * @param g2d Graphics2D object
     * @param metrics FontMetrics object
     * @param text Text to draw
     * @param x X-coordinate
     * @param y Y-coordinate
     * @param maxWidth Maximum width for wrapped text
     * @param lineHeight Line height
     * @return Y-coordinate after drawing the text
     */
    private static int drawWrappedText(Graphics2D g2d, FontMetrics metrics, String text,
                                       int x, int y,
                                       int maxWidth, int lineHeight) {
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();

        for (String word : words) {
            if (metrics.stringWidth(line + word) <= maxWidth) {
                line.append(word).append(" ");
            } else {
                g2d.drawString(line.toString(), x, y);
                y += lineHeight;
                line = new StringBuilder(word).append(" ");
            }
        }

        if (!line.isEmpty()) {
            g2d.drawString(line.toString(), x, y);
            y += lineHeight;
        }

        return y;
    }

    /***
     * This method to convert buffered image to base64
     * @param image BufferedImage
     * @return base64 string
     */
    public static String imageToBase64(BufferedImage image) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", outputStream);
            byte[] imageBytes = outputStream.toByteArray();
            return Base64.getEncoder().encodeToString(imageBytes);
        } catch (IOException e) {
            return null;
        }
    }


    public static void createSignatureImage(PdfCanvas canvas, Rectangle2D.Float signatureRectangle,
                                            List<String> textList, String base64Image) throws IOException {
        byte[] imageBytes = Base64.getDecoder().decode(base64Image);
        // Dimensions of the signature rectangle
        float x = signatureRectangle.x;
        float y = signatureRectangle.y;
        float width = signatureRectangle.width;
        float height = signatureRectangle.height;

        // Split rectangle into sections (image 30%, text 70%)
        float imageHeight = height * 0.3f;
        float textHeight = height * 0.7f;

        // Draw the image (optional)
        if (nonNull(base64Image)) {
            ImageData imageData = ImageDataFactory.create(imageBytes);
            Image image = new Image(imageData);
            image.setFixedPosition(x, y + textHeight);
            image.scaleToFit(width, imageHeight);
            canvas.addImageAt(imageData, x, y + textHeight, false);
        }

        // Draw text information
        float textY = y + 5; // Adjust padding
        canvas.setFontAndSize(PdfFontFactory.createFont(StandardFonts.HELVETICA), 10);
        canvas.setFillColor(ColorConstants.BLACK);

        for (String text : textList) {
            canvas.beginText();
            canvas.moveText(x + 5, textY + textHeight - 15);
            canvas.showText(text);
            canvas.endText();
            textY -= 15; // Move up for the next line
        }

        canvas.stroke();
    }


    public static void createSignatureImage(PdfPage page, Rectangle signatureRectangle,
                                            List<String> textList, String base64Image) throws IOException {
        PdfCanvas canvas = new PdfCanvas(page);

        // Decode the base64 image
        byte[] imageBytes = null;
        if (base64Image != null && !base64Image.isEmpty()) {
            try {
                imageBytes = Base64.getDecoder().decode(base64Image);
            } catch (IllegalArgumentException e) {
                throw new IOException("Invalid base64 image data", e);
            }
        }

        // Divide the rectangle: 60% for image, 40% for text
        float imageHeight = signatureRectangle.getHeight() * 0.6f;
        float textHeight = signatureRectangle.getHeight() * 0.4f;

        // Calculate positions
        float imageX = signatureRectangle.getX() + 5; // Add padding
        float imageY = signatureRectangle.getY() + textHeight; // Image starts above text
        float imageWidth = signatureRectangle.getWidth() - 10; // Account for padding
        float textX = signatureRectangle.getX() + 5; // Add padding for text
        float textY = signatureRectangle.getY() + textHeight - 5; // Text starts at the bottom
        float padding = 10; // Padding for text

        // Draw the image (if available)
        if (imageBytes != null) {
            try {
                ImageData imageData = ImageDataFactory.create(imageBytes);
                canvas.addImageWithTransformationMatrix(imageData, imageWidth, 0, 0, imageHeight - 5, imageX, imageY);
            } catch (Exception e) {
                throw new IOException("Failed to render image", e);
            }
        }
        PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA);

        // Adjust font size dynamically
        float fontSize = adjustFontSizeForText(font, textList, textHeight, padding);
        System.out.println("Font size: " + fontSize);

        float x = textX + padding; // Starting X position with padding
        float y = textY - padding; // Start at the top of the rectangle

        canvas.beginText()
                .setFontAndSize(font, fontSize)
                .setColor(ColorConstants.BLACK, true)
                .moveText(x, y);


        // Draw each text line
        for (String text : textList) {

            canvas.showText(text).moveText(0, -fontSize); // Line spacing
//            y -= fontSize; // Move down for the next line
        }

        canvas.endText();
        // Draw the text
//        canvas.beginText()
//                .setFontAndSize(PdfFontFactory.createFont(StandardFonts.HELVETICA), 10)
//                .setColor(ColorConstants.BLACK, true);
//        canvas.moveText(textX, textY);
//        for (String text : textList) {
////            if (textY < signatureRectangle.getY()) {
////                // Skip text that doesn't fit in the allocated area
////                break;
////            }
//            canvas.showText(text)
//                    .moveText(0, -12); // Line spacing
////            textY -= 12; // Update text position
//        }
//
//        canvas.endText();

        // Draw a border for debugging (optional)
        canvas.setLineWidth(1)
                .setStrokeColor(ColorConstants.RED)
                .rectangle(signatureRectangle)
                .stroke();

        // Draw additional borders if needed
        canvas.setLineWidth(1)
                .setStrokeColor(ColorConstants.BLACK)
                .rectangle(new Rectangle(signatureRectangle.getX(), imageY,
                        signatureRectangle.getWidth(), imageHeight))
                .stroke();

        canvas.setLineWidth(1)
                .setStrokeColor(ColorConstants.YELLOW)
                .rectangle(new Rectangle(signatureRectangle.getX() + 5, imageY,
                        imageWidth, imageHeight - 10))
                .stroke();
    }

    private static float adjustFontSizeForText(PdfFont font, List<String> textList, float rectangleHeight, float padding) throws IOException {
        float fontSize = 12; // Start with a default font size
        float availableHeight = rectangleHeight - (2 * padding);

        while (true) {
            // Calculate total text height
            float totalTextHeight = textList.size() * fontSize;

            if (totalTextHeight > availableHeight) {
                // Reduce font size if the text height exceeds available height
                fontSize -= 0.5f;
            } else if (totalTextHeight < availableHeight * 0.8f && fontSize < 20) { // Limit maximum font size
                // Increase font size if there is too much free space
                fontSize += 0.5f;
            } else {
                // Font size is optimal
                break;
            }

            if (fontSize < 5) {
                // Stop reducing if font size is too small
                break;
            }
        }

        return fontSize;
    }


}
