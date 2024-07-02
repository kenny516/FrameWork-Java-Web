package Utils;

import Annotation.Param;
import jakarta.servlet.http.HttpServletRequest;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Enumeration;

public class Requestparam {
    HttpServletRequest request;

    public Requestparam(HttpServletRequest request) {
        this.request = request;
    }

    public Object mappingParam(Parameter param,String name) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Enumeration<String> parametersNames = request.getParameterNames();
        ArrayList<String> listOfparamObj = new ArrayList<>();
        // verification si c est un objet
        // Parcourir tous les paramètres de la requête
        while (parametersNames.hasMoreElements()) {
            String nameParam = parametersNames.nextElement();
            String[] valueName = nameParam.split("\\.");

            // Vérifier si le paramètre appartient à un objet
            if (valueName.length > 1) {
                if (valueName[0].equals(name)) {
                    listOfparamObj.add(nameParam);
                } else if (param.isAnnotationPresent(Param.class)) {
                    if (valueName[0].equals(param.getAnnotation(Param.class).name())) {
                        listOfparamObj.add(nameParam);
                    }
                }
            }
        }
        //si c est un objet
        if (listOfparamObj.size() > 0) {
            Object paramInstance = param.getType().getDeclaredConstructor().newInstance();
            Method[] methods = param.getType().getDeclaredMethods();

            for (String paramObj : listOfparamObj) {
                String paramObjGET = request.getParameter(paramObj);
                for (Method method : methods) {
                    if (method.getName().equals("set" +  Tools.capitalize(paramObj.split("\\.")[1])))  {
                        if (method.getParameters()[0].getType() == int.class) {
                            method.invoke(paramInstance, Integer.valueOf(paramObjGET));
                        } else {
                            method.invoke(paramInstance,paramObjGET);
                        }
                    }
                }
            }
            return paramInstance;
        }
        //sinon
        else {
            if (request.getParameter(name) != null) {
                return request.getParameter(name);
            }
            //avec annotation
            else if (param.isAnnotationPresent(Param.class)) {
                if (request.getParameter(param.getAnnotation(Param.class).name()) != null) {
                    return request.getParameter(param.getAnnotation(Param.class).name());
                } else {
                    return null;
                }
            }
            return null;
        }
    }

    public Object castParam(String parameter, Class<?> clazz) {
        Object retour = new Object();
        if (clazz == int.class) {
            retour = Integer.valueOf(parameter);
        } else {
            retour = parameter;
        }
        return retour;
    }

}
