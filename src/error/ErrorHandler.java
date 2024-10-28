package error;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ErrorHandler {
    private static final Gson gson = new Gson();

    private static String buildErrorPage(Exception e, int statusCode) {
        LocalDateTime now = LocalDateTime.now();
        String formattedDate = now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Error %d - Server Error</title>
                <style>
                    :root {
                        --primary-color: #2d3748;
                        --error-color: #e53e3e;
                        --background-color: #f7fafc;
                        --card-background: #ffffff;
                        --text-color: #4a5568;
                        --border-color: #edf2f7;
                    }
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;
                        line-height: 1.6;
                        margin: 0;
                        padding: 0;
                        background-color: var(--background-color);
                        color: var(--text-color);
                        min-height: 100vh;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                    }
                    .error-container {
                        background-color: var(--card-background);
                        border-radius: 8px;
                        box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
                        padding: 2rem;
                        max-width: 800px;
                        width: 90%%;
                        margin: 2rem;
                    }
                    
                    .error-header {
                        display: flex;
                        align-items: center;
                        margin-bottom: 1.5rem;
                        padding-bottom: 1rem;
                        border-bottom: 2px solid var(--border-color);
                    }
                    
                    .error-icon {
                        background-color: #FEE2E2;
                        color: var(--error-color);
                        padding: 0.75rem;
                        border-radius: 50%%;
                        margin-right: 1rem;
                    }
                    
                    .error-title {
                        color: var(--primary-color);
                        margin: 0;
                        font-size: 1.5rem;
                        font-weight: 600;
                    }
                    
                    .error-code {
                        color: var(--error-color);
                        font-size: 1.125rem;
                        font-weight: 500;
                        margin-bottom: 1rem;
                    }
                    
                    .error-message {
                        background-color: #FEF2F2;
                        border: 1px solid #FEE2E2;
                        border-radius: 6px;
                        padding: 1rem;
                        margin-bottom: 1.5rem;
                        color: #991B1B;
                    }
                    
                    .error-details {
                        background-color: #F8FAFC;
                        border-radius: 6px;
                        padding: 1rem;
                        margin-bottom: 1rem;
                    }
                    
                    .error-details h3 {
                        margin-top: 0;
                        color: var(--primary-color);
                        font-size: 1rem;
                    }
                    
                    .error-timestamp {
                        color: #718096;
                        font-size: 0.875rem;
                        margin-top: 1.5rem;
                        padding-top: 1rem;
                        border-top: 1px solid var(--border-color);
                    }
                    
                    .error-actions {
                        margin-top: 1.5rem;
                        display: flex;
                        gap: 1rem;
                    }
                    
                    .button {
                        display: inline-flex;
                        align-items: center;
                        padding: 0.5rem 1rem;
                        border-radius: 4px;
                        font-weight: 500;
                        text-decoration: none;
                        transition: all 0.2s;
                    }
                    
                    .button-primary {
                        background-color: var(--primary-color);
                        color: white;
                    }
                    
                    .button-primary:hover {
                        background-color: #1a202c;
                    }
                    
                    @media (max-width: 640px) {
                        .error-container {
                            margin: 1rem;
                            padding: 1.5rem;
                        }
                        
                        .error-actions {
                            flex-direction: column;
                        }
                    }
                </style>
            </head>
            <body>
                <div class="error-container">
                    <div class="error-header">
                        <div class="error-icon">
                            ⚠️
                        </div>
                        <h1 class="error-title">Server Error</h1>
                    </div>
                    
                    <div class="error-code">
                        Error %d
                    </div>
                    
                    <div class="error-message">
                        <strong>Error Message:</strong> %s
                    </div>
                    
                    <div class="error-details">
                        <h3>Technical Details</h3>
                        <p>An unexpected error occurred while processing your request. Our team has been notified and is working to resolve the issue.</p>
                        <p><strong>Error Type:</strong> %s</p>
                    </div>
                    
                    <div class="error-actions">
                        <a href="/" class="button button-primary">Return to Home</a>
                    </div>
                    
                    <div class="error-timestamp">
                        Error occurred at: %s
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                statusCode,
                statusCode,
                e.getMessage(),
                e.getClass().getSimpleName(),
                formattedDate
        );
    }

    public static void handleException(HttpServletRequest req, HttpServletResponse res, Exception e) throws IOException {
        int statusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        res.setStatus(statusCode);
        res.setContentType("text/html");
        res.setCharacterEncoding("UTF-8");

        String errorPage = buildErrorPage(e, statusCode);
        res.getWriter().println(errorPage);
    }

    public static void handleRestApiException(HttpServletResponse res, Exception e) throws IOException {
        int statusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        res.setStatus(statusCode);
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");

        Map<String, Object> errorResponse = new HashMap<>();
        Map<String, Object> errorDetails = new HashMap<>();

        // Enhanced error details
        errorDetails.put("timestamp", LocalDateTime.now().toString());
        errorDetails.put("status", statusCode);
        errorDetails.put("error", "Internal Server Error");
        errorDetails.put("message", e.getMessage());
        errorDetails.put("type", e.getClass().getSimpleName());
        errorDetails.put("path", res.getHeader("X-Original-URL"));

        if (isDevelopmentMode()) {
            String stackTrace = String.valueOf(java.util.Arrays.stream(e.getStackTrace())
                    .map(StackTraceElement::toString)
                    .limit(5)
                    .toList());
            errorDetails.put("stackTrace", stackTrace);
        }

        errorResponse.put("error", errorDetails);

        PrintWriter out = res.getWriter();
        out.print(gson.toJson(errorResponse));
        out.flush();
    }

    private static boolean isDevelopmentMode() {
        return System.getProperty("app.environment", "development")
                .equalsIgnoreCase("development");
    }
}
