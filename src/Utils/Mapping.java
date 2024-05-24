package Utils;

public class Mapping {
    String class_name;
    String method_name;

    public Mapping(String class_name, String method_name){
        this.setClass_name(class_name);
        this.setMethod_name(method_name);
    }

    public String getMethod_name() {
        return method_name;
    }

    public void setMethod_name(String method_name) {
        this.method_name = method_name;
    }


    public String getClass_name() {
        return class_name;
    }

    public void setClass_name(String class_name) {
        this.class_name = class_name;
    }
}
