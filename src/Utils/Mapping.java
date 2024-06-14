package Utils;

import java.lang.reflect.Method;

public class Mapping {
    String class_name;
    Method method;

    public Mapping(String class_name, Method method){
        this.setClass_name(class_name);
        this.setMethod(method);
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public String getClass_name() {
        return class_name;
    }

    public void setClass_name(String class_name) {
        this.class_name = class_name;
    }
}
