package Utils;

import Annotation.Param;
import Annotation.validation.Validator;
import Utils.UploadFile.UploadFile;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Enumeration;

public class Requestparam {
    private final HttpServletRequest request;

    public Requestparam(HttpServletRequest request) {
        this.request = request;
    }

    public Object mappingParam(Parameter param, String name) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, ServletException, IOException {
        ArrayList<String> listOfParamObj = collectObjectParameters(param, name);

        if (param.getType() == UploadFile.class) {
//            throw new ServletException("ici "+param.getAnnotation(Param.class).name() + " "+request.getPart(param.getAnnotation(Param.class).name()));
            if (UploadFile.isUpload(request, param.getAnnotation(Param.class).name())) {
                return new UploadFile(request.getPart(param.getAnnotation(Param.class).name()));
            }else {
                throw new ServletException("No file uploaded or attribute name is incorrect for => "+param.getAnnotation(Param.class).name());
            }
        }


        // Check if it's an object
        if (!listOfParamObj.isEmpty()) {
            Object paramInstance = param.getType().getDeclaredConstructor().newInstance();
            Method[] methods = param.getType().getDeclaredMethods();

            for (String paramObj : listOfParamObj) {
                String paramValue = request.getParameter(paramObj);
                String fieldName = paramObj.split("\\.")[1];
                Method setterMethod = findSetterMethod(methods, fieldName);

                if (setterMethod != null) {
                    Class<?> paramType = setterMethod.getParameters()[0].getType();
                    Object castedValue = castValue(paramValue, paramType);

                    setterMethod.invoke(paramInstance, castedValue);
                }
            }
            Validator.validate(paramInstance,request);
            return paramInstance;
        }

        // Otherwise, handle it as a basic type or annotated parameter
        return getParameterValue(param, name);
    }

    private ArrayList<String> collectObjectParameters(Parameter param, String name) {
        ArrayList<String> listOfParamObj = new ArrayList<>();
        Enumeration<String> parameterNames = request.getParameterNames();

        // Collect parameters that belong to the object
        while (parameterNames.hasMoreElements()) {
            String paramName = parameterNames.nextElement();
            String[] valueNameParts = paramName.split("\\.");

            if (valueNameParts.length > 1 && valueNameParts[0].equals(name)) {
                listOfParamObj.add(paramName);
            } else if (param.isAnnotationPresent(Param.class) && valueNameParts[0].equals(param.getAnnotation(Param.class).name())) {
                listOfParamObj.add(paramName);
            }
        }
        return listOfParamObj;
    }

    private Method findSetterMethod(Method[] methods, String fieldName) {
        String expectedMethodName = "set" + Tools.capitalize(fieldName);
        for (Method method : methods) {
            if (method.getName().equals(expectedMethodName)) {
                return method;
            }
        }
        return null; // If no setter found
    }

    private Object castValue(String value, Class<?> paramType) {
        if (paramType == int.class || paramType == Integer.class) {
            return Integer.valueOf(value);
        } else if (paramType == double.class || paramType == Double.class) {
            return Double.valueOf(value);
        } else if (paramType == long.class || paramType == Long.class) {
            return Long.valueOf(value);
        } else if (paramType == boolean.class || paramType == Boolean.class) {
            return Boolean.valueOf(value);
        } else if (paramType == float.class || paramType == Float.class) {
            return Float.valueOf(value);
        } else if (paramType == short.class || paramType == Short.class) {
            return Short.valueOf(value);
        } else if (paramType == byte.class || paramType == Byte.class) {
            return Byte.valueOf(value);
        } else {
            // For String and other types
            return value;
        }
    }

    private Object getParameterValue(Parameter param, String name) {
        // Check if the parameter exists in the request
        if (request.getParameter(name) != null) {
            return request.getParameter(name);
        }
        // Handle annotated parameter
        if (param.isAnnotationPresent(Param.class)) {
            String paramName = param.getAnnotation(Param.class).name();
            return request.getParameter(paramName);
        }

        return null;
    }
}
