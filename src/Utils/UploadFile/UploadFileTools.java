package Utils.UploadFile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;

public class UploadFileTools {
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
}
