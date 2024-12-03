package vn.com.dattb.utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;

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
}
