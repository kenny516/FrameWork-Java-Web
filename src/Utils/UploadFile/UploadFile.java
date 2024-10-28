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
    public static boolean isUpload(HttpServletRequest request, String name) {
        try {
            Part part = request.getPart(name);
            return part != null && part.getSize() > 0; // Ensure part has content
        } catch (Exception e) {
            // Log exception for debugging
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
     * @param part The uploaded file part
     * @param uploadDir The directory to save the file to
     * @return The path of the saved file, or null if save failed
     */
    public static String saveFile(Part part, String uploadDir) throws IOException {
        String fileName = extractFileName(part);
        if (fileName == null || fileName.isEmpty()) {
            throw new IOException("Invalid file name");
        }

        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        String uniqueFileName = System.currentTimeMillis() + "_" + fileName;
        Path filePath = Paths.get(uploadDir, uniqueFileName);

        try (InputStream input = part.getInputStream()) {
            Files.copy(input, filePath, StandardCopyOption.REPLACE_EXISTING);
            return filePath.toString();
        } catch (IOException e) {
            throw new IOException("Failed to save file: " + e.getMessage(), e);
        }
    }

    /**
     * Gets the byte array of an uploaded file
     * @param part The uploaded file part
     * @return byte array containing the file data
     */
    public static byte[] getBytes(Part part) throws IOException {
        try (InputStream input = part.getInputStream()) {
            return input.readAllBytes();
        } catch (IOException e) {
            throw new IOException("Failed to read file bytes: " + e.getMessage(), e);
        }
    }
}
