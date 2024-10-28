package Utils.UploadFile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class UploadFile {
    Part part;

    public UploadFile(Part part) {
        this.part = part;
    }

    public Part getPart() {
        return part;
    }

    public void setPart(Part part) {
        this.part = part;
    }

    public static boolean isUpload(HttpServletRequest request, String name) {
        try {
            Part part = request.getPart(name);
            return part != null && part.getSize() > 0; // Ensure part has content
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String extractFileName(Part part) {
        String contentDisposition = part.getHeader("content-disposition");
        String[] tokens = contentDisposition.split(";");
        for (String token : tokens) {
            if (token.trim().startsWith("filename")) {
                return token.substring(token.indexOf('=') + 2, token.length() - 1);
            }
        }
        return null;
    }


    /**
     * Saves an uploaded file to the specified directory
     * @param uploadDir The directory to save the file to do not put just / or \ as it will save to the root directory put a name of directory or..
     *                  the racine is tomcat
     * @return The path of the saved file
     * @throws IOException if file operations fail
     * @throws Exception if validation fails
     */
    public String saveFile(String uploadDir) throws Exception {
        Path rootPath = (uploadDir == null || uploadDir.isEmpty())
                ? Paths.get("").toAbsolutePath().resolve("uploads")
                : Paths.get(uploadDir).toAbsolutePath();

        String fileName = extractFileName(this.getPart());
        if (fileName == null || fileName.isEmpty()) {
            throw new Exception("File does not have a name");
        }

        String uniqueFileName = System.currentTimeMillis() + "_" + fileName;
        Path filePath = rootPath.resolve(uniqueFileName);
        Files.createDirectories(rootPath);

        if (!Files.isWritable(rootPath)) {
            throw new Exception("Access denied - Cannot write to directory: " + rootPath.toString());
        }

        part.write(filePath.toString());
        return filePath.toString();
    }


    /**
     * Gets the byte array of an uploaded file
     * @return byte array containing the file data
     */
    public byte[] getBytes() throws IOException {
        try (InputStream input = this.getPart().getInputStream()) {
            return input.readAllBytes();
        } catch (IOException e) {
            throw new IOException("Failed to read file bytes: " + e.getMessage(), e);
        }
    }
}
