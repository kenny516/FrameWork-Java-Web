package Model;

import jakarta.servlet.http.HttpSession;

import java.util.HashMap;

public class CustomSession {
    private HttpSession httpSession;

    public HttpSession getHttpSession() {
        return httpSession;
    }

    public void setHttpSession(HttpSession httpSession) {
        this.httpSession = httpSession;
    }

    public CustomSession(HttpSession httpSession) {
        this.httpSession = httpSession;
    }
    public CustomSession() {
    }


    public void addSession(String key, Object value) {
        httpSession.setAttribute(key, value);
    }

    public Object getAttribute(String key) {
        return httpSession.getAttribute(key);
    }

    public void removeAttribute(String key) {
        httpSession.removeAttribute(key);
    }
}
