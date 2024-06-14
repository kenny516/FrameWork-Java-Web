package Utils;

import Annotation.Param;
import jakarta.servlet.http.HttpServletRequest;

import java.lang.reflect.Parameter;

public class Requestparam {
    HttpServletRequest request;

    public Object mappingParam(Parameter param){

        if (request.getParameter(param.getName()) !=null){
            return request.getParameter(param.getName());
        } else if (param.getClass().isAnnotationPresent(Param.class)) {
            if (request.getParameter(param.getClass().getAnnotation(Param.class).name()) != null){
                return request.getParameter(param.getName());
            }else {
                return null;
            }
        }
        return null;
    }

    public Requestparam(HttpServletRequest request) {
        this.request = request;
    }
}
