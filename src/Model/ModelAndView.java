package Model;

import jakarta.servlet.ServletException;

import java.util.HashMap;

public class ModelAndView {
    String url;
    HashMap<String, Object> data;
    Boolean isRedirect = false;

    public ModelAndView(String url) {
        this.setUrl(url);
        this.setData(new HashMap<>());
    }

    public ModelAndView() {
        this.setData(new HashMap<>());
    }

    public HashMap<String, Object> getData() {
        return data;
    }

    public void setData(HashMap<String, Object> data) {
        this.data = data;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Boolean getIsRedirect() {
        return isRedirect;
    }

    public void setIsRedirect(Boolean redirect) {
        isRedirect = redirect;
    }


    /**
     * Adds a key-value pair to the model's data map.
     *
     * @param name  The key for the data entry
     * @param value The value to be associated with the key
     * @throws Exception if the view URL is null
     */
    public void add_data(String name, Object value) throws Exception {
        if (this.url == null) {
            throw new Exception("url of ModelAndview is null");
        }
        this.data.put(name, value);
    }
}
