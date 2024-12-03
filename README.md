# Draw text to image, add text to image.
This is a library that helps you add text to existing images, 
which can be used in digital signatures or simply editing PDF documents. More specifically, you are working with pdf using itext, itex7 or pdfbox to process text, handle adding images to text or add images as a signature. 

## Features
- Add text to image

## Installation
```bash
./gradlew clean build
```

## Usage
```java
class Main {
    public static void main(String[] args) {
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
```
## Result
From the image below, you can see the text added to the image.
### Original image
![image](src/main/resources/original_signature.png)
### Image after adding text, if you don't see the text, please turn off the dark mode of your browser
![image](/added_text_original_signature.png)



## License
[Apache](https://www.apache.org/licenses/LICENSE-2.0)

## Contributing
Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

## Changelog
All change logs for this project are documented in [changelog.md](changelog.md) file.

## Authors
- Dat Tran Ba
- [Website](https://dattb.com)
- [Github](https://github.com/DatTran98)
- [Linkedin](https://www.linkedin.com/in/dat-tran-ba/)
- [Facebook](https://www.facebook.com/dat.tbit)
