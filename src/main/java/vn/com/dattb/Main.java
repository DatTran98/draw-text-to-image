package vn.com.dattb;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Base64;
import java.util.Map;

import static vn.com.dattb.utils.ImageTextUtils.addInfoToBase64Image;
import static vn.com.dattb.utils.ImageTextUtils.imageToBase64;

public class Main {
    public static void main(String[] args) {
        testAddTextToImage();
    }

    private static void testAddTextToImage() {
        Map<String, String> infoMap = Map.of(
                "Name", "Dat Tran Ba",
                "Position", "Software Engineer",
                "Company", "dattb.com Vietnam"
        );
        // Read an image from the resources folder
        try (InputStream inputStream = Main.class.getResourceAsStream("/original_signature.png")) {
            if (inputStream == null) {
                System.out.println("File not found in resources folder");
                return;
            }
            BufferedImage image = ImageIO.read(inputStream);

            //convert image to base64 because in some cases we need to send image as base64 string in request body
            String base64Image = imageToBase64(image);
            String updatedBase64Image = addInfoToBase64Image(base64Image, infoMap);

            // Save the image to the file system
            File file = new File("added_text_original_signature.png");
            byte[] imageBytes = Base64.getDecoder().decode(updatedBase64Image);
            BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
            try (FileOutputStream fos = new FileOutputStream(file)) {
                ImageIO.write(bufferedImage, "png", fos);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}