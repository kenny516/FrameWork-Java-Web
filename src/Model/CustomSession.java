package Model;

import java.util.HashMap;

public class CustomSession {
    private HashMap<String, Object> session = new HashMap<>();

    public CustomSession(HashMap<String, Object> sessionMap) {
        this.session = sessionMap;
    }
    public CustomSession() {
    }

    public void setSession(HashMap<String, Object> session) {
        this.session = session;
    }

    public HashMap<String, Object> getSession() {
        return session;
    }

    public void addSession(String key, Object value) {
        session.put(key, value);
    }

    public boolean removeSession(String key) {
        return session.remove(key) != null;
    }
}
