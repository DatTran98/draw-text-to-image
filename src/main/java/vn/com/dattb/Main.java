package vn.com.dattb;

import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static vn.com.dattb.utils.ImageTextUtils.*;

public class Main {
    public static void main(String[] args) {
//        testAddTextToImage();
        testDrawTextOnImageUsingCanvas();
    }

    private static void testDrawTextOnImageUsingCanvas() {
        List<String> texts = List.of(
                "Name: Dat Tran Ba",
                "Position: Software Engineer",
                "Company: dattb.com Vietnam"
        );

        // Read resources
        try (InputStream inputStream = Main.class.getResourceAsStream("/original_signature.png");
             InputStream inputStreamPdf = Main.class.getResourceAsStream("/signed_appearance_test.pdf")) {

            if (inputStream == null) {
                System.err.println("Image file not found in resources folder");
                return;
            }
            if (inputStreamPdf == null) {
                System.err.println("PDF file not found in resources folder");
                return;
            }

            // Convert image to Base64
            BufferedImage image = ImageIO.read(inputStream);
            String base64Image = imageToBase64(image);
            System.out.println("Base64 Image: " + base64Image);

            // Prepare PDF document
            OutputStream outputStream = new ByteArrayOutputStream();
            PdfReader reader = new PdfReader(inputStreamPdf);
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDoc = new PdfDocument(reader, writer);

            PdfPage page = pdfDoc.getFirstPage();
            Rectangle rectangle = new Rectangle(100, 100, 200, 200);

            // Draw on PDF
            createSignatureImage(page, rectangle, texts, base64Image);

            // Close PDF document
            pdfDoc.close();

            // Save the output file
            File file = new File("added_text_original_signature.pdf");
            try (FileOutputStream fos = new FileOutputStream(file)) {
                ((ByteArrayOutputStream) outputStream).writeTo(fos);
            }

            System.out.println("PDF successfully created: " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error while processing PDF", e);
        }
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