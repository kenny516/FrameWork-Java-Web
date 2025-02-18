package error;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

public class ErrorHandler {
    private static final Gson gson = new Gson();

    public static void handleException(HttpServletRequest req, HttpServletResponse res, Exception e) throws IOException {
        int statusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        res.setStatus(statusCode);
        res.setContentType("text/html");
        res.setCharacterEncoding("UTF-8");

        String errorPage = buildErrorPage(req, e, statusCode);
        res.getWriter().println(errorPage);
    }

    public static void handleRestApiException(HttpServletRequest req, HttpServletResponse res, Exception e) throws IOException {
        int statusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        res.setStatus(statusCode);
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", buildErrorDetails(req, e, statusCode));

        try (PrintWriter out = res.getWriter()) {
            out.print(gson.toJson(errorResponse));
        }
    }

    private static String buildErrorPage(HttpServletRequest req, Exception e, int statusCode) {
        LocalDateTime now = LocalDateTime.now();
        String formattedDate = now.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        String stackTrace = getFilteredStackTrace(e);
        boolean isDevMode = isDevelopmentMode();
        String requestId = generateRequestId();

        // Définir stackTraceDiv pour afficher ou non la stack trace selon le mode de développement
        String stackTraceDiv = isDevMode
                ? "<div class='stack-trace'><h3>Stack Trace</h3><pre>" + escapeHtml(stackTrace) + "</pre></div>"
                : "";

        return String.format("""
                        <!DOCTYPE html>
                        <html lang="fr">
                        <head>
                            <meta charset="UTF-8">
                            <meta name="viewport" content="width=device-width, initial-scale=1.0">
                            <title>Erreur %d</title>
                            <style>
                                :root {
                                    --primary-color: #2563eb;
                                    --error-color: #dc2626;
                                    --background: #f8fafc;
                                    --card-bg: #ffffff;
                                    --text: #1e293b;
                                }
                        
                                body {
                                    font-family: system-ui, -apple-system, sans-serif;
                                    line-height: 1.5;
                                    margin: 0;
                                    padding: 1rem;
                                    background: var(--background);
                                    color: var(--text);
                                    min-height: 100vh;
                                    display: grid;
                                    place-items: center;
                                }
                        
                                .error-card {
                                    background: var(--card-bg);
                                    border-radius: 0.5rem;
                                    box-shadow: 0 1px 3px rgba(0,0,0,0.1);
                                    padding: 1.5rem;
                                    max-width: 800px;
                                    width: 100%%;
                                }
                        
                                .error-header {
                                    display: flex;
                                    align-items: center;
                                    gap: 1rem;
                                    margin-bottom: 1.5rem;
                                }
                        
                                .error-icon {
                                    font-size: 2rem;
                                    color: var(--error-color);
                                }
                        
                                .error-title {
                                    margin: 0;
                                    color: var(--text);
                                    font-size: 1.5rem;
                                }
                        
                                .error-meta {
                                    display: flex;
                                    flex-wrap: wrap;
                                    gap: 1rem;
                                    margin-bottom: 1.5rem;
                                }
                        
                                .meta-item {
                                    background: #f1f5f9;
                                    border-radius: 0.25rem;
                                    padding: 0.5rem 1rem;
                                    flex: 1 1 200px;
                                }
                        
                                .error-details {
                                    margin: 1.5rem 0;
                                }
                        
                                .stack-trace {
                                    background: #f8fafc;
                                    border: 1px solid #e2e8f0;
                                    border-radius: 0.25rem;
                                    padding: 1rem;
                                    font-family: monospace;
                                    white-space: pre-wrap;
                                    max-height: 300px;
                                    overflow-y: auto;
                                }
                        
                                .actions {
                                    display: flex;
                                    flex-wrap: wrap;
                                    gap: 1rem;
                                    margin-top: 2rem;
                                }
                        
                                .button {
                                    padding: 0.5rem 1rem;
                                    border-radius: 0.25rem;
                                    text-decoration: none;
                                    display: inline-flex;
                                    align-items: center;
                                    gap: 0.5rem;
                                    transition: all 0.2s;
                                    flex: 1 1 auto;
                                }
                        
                                .primary {
                                    background: var(--primary-color);
                                    color: white;
                                }
                        
                                .secondary {
                                    background: #e2e8f0;
                                    color: var(--text);
                                }
                        
                                .button:hover {
                                    filter: brightness(0.9);
                                }
                        
                                .copy-success {
                                    color: #16a34a;
                                    font-size: 0.875rem;
                                    margin-left: 1rem;
                                    opacity: 0;
                                    transition: opacity 0.3s;
                                }
                        
                                .visible {
                                    opacity: 1;
                                }
                        
                                @media (max-width: 600px) {
                                    .error-card {
                                        padding: 1rem;
                                    }
                        
                                    .error-meta {
                                        flex-direction: column;
                                    }
                        
                                    .meta-item {
                                        flex: 1 1 100%%;
                                    }
                        
                                    .actions {
                                        flex-direction: column;
                                    }
                                }
                            </style>
                        </head>
                        <body>
                            <div class="error-card">
                                <div class="error-header">
                                    <div class="error-icon">⚠️</div>
                                    <h1 class="error-title">Erreur du serveur</h1>
                                </div>
                        
                                <div class="error-meta">
                                    <div class="meta-item">
                                        <strong>Code :</strong> %d
                                    </div>
                                    <div class="meta-item">
                                        <strong>Date :</strong> %s
                                    </div>
                                    <div class="meta-item">
                                        <strong>Requête ID :</strong> %s
                                    </div>
                                </div>
                        
                                <div class="error-details">
                                    <h3>Message d'erreur</h3>
                                    <p>%s</p>
                                </div>
                        
                                %s
                        
                                <div class="actions">
                                    <a href="/" class="button primary">Retour à l'accueil</a>
                                    <button onclick="copyError()" class="button secondary">
                                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                                            <path d="M8 4v12h12V4H8zM6 2h12a2 2 0 0 1 2 2v12a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2z"/>
                                            <path d="M16 8h4v12H8v-4"/>
                                        </svg>
                                        Copier les détails
                                    </button>
                                    <span id="copySuccess" class="copy-success">Copié !</span>
                                </div>
                            </div>
                        
                            <script>
                                function copyError() {
                                    const details = `Code d'erreur: %d\\nDate: %s\\nID Requête: %s\\nMessage: %s\\nType: %s\\nStackTrace: %s`;
                        
                                    navigator.clipboard.writeText(details)
                                        .then(() => showCopySuccess())
                                        .catch(err => console.error('Erreur de copie:', err));
                                }
                        
                                function showCopySuccess() {
                                    const success = document.getElementById('copySuccess');
                                    success.classList.add('visible');
                                    setTimeout(() => {
                                        success.classList.remove('visible');
                                    }, 2000);
                                }
                            </script>
                        </body>
                        </html>
                        """,
                statusCode,                     // %d (titre)
                statusCode,                     // %d (bloc meta - Code)
                formattedDate,                  // %s (bloc meta - Date)
                requestId,                      // %s (bloc meta - Requête ID)
                escapeHtml(e.getMessage()),     // %s (bloc meta - Message d'erreur)
                stackTraceDiv,                  // %s (bloc stack trace)
                statusCode,                     // %d (script - Code d'erreur)
                formattedDate,                  // %s (script - Date)
                requestId,                      // %s (script - ID Requête)
                escapeJs(e.getMessage()),       // %s (script - Message)
                escapeJs(e.getClass().getSimpleName()), // %s (script - Type)
                escapeJs(stackTrace)           // %s (script - StackTrace)
        );
    }


    private static Map<String, Object> buildErrorDetails(HttpServletRequest req, Exception e, int statusCode) {
        Map<String, Object> details = new HashMap<>();
        details.put("timestamp", LocalDateTime.now().toString());
        details.put("status", statusCode);
        details.put("error", e.getClass().getSimpleName());
        details.put("message", e.getMessage());
        details.put("path", req.getRequestURI());
        details.put("requestId", generateRequestId());

        if (isDevelopmentMode()) {
            details.put("stackTrace", Arrays.stream(e.getStackTrace())
                    .limit(10)
                    .map(StackTraceElement::toString)
                    .collect(Collectors.toList()));
        }

        return details;
    }

    private static String getFilteredStackTrace(Exception e) {
        return Arrays.stream(e.getStackTrace())
                .limit(15)
                .map(StackTraceElement::toString)
                .collect(Collectors.joining("\n"));
    }

    private static String generateRequestId() {
        return Long.toHexString(System.currentTimeMillis()) + "-" +
                (int) (Math.random() * 1000);
    }

    private static String escapeHtml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private static String escapeJs(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                .replace("'", "\\'")
                .replace("\"", "\\\"")
                .replace("\n", "\\n");
    }

    private static boolean isDevelopmentMode() {
        return System.getProperty("app.environment", "development")
                .equalsIgnoreCase("development");
    }
}
