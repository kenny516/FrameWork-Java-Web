package Model;

import jakarta.servlet.ServletException;

import java.util.HashMap;

public class ModelAndView {
    String url;
    HashMap<String, Object> data;

    public ModelAndView(String url){
        this.setUrl(url);
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

    public void setUrl(String url) {this.url = url; }

    public void add_data(String name, Object value) throws ServletException {
        if (this.data.get(url) == null){
            throw new ServletException("url");
        }
        this.data.put(name, value);


    }
}
