package Controller;

import Annotation.*;
import Model.CustomSession;
import Model.ModelAndView;
import Utils.AccesController;
import Utils.Mapping;
import Utils.Requestparam;
import Utils.Tools;
import com.google.gson.Gson;
import com.thoughtworks.paranamer.BytecodeReadingParanamer;
import com.thoughtworks.paranamer.Paranamer;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FrontController extends HttpServlet {
    Gson json = new Gson();
    HashMap<String, Mapping> road_controller = new HashMap<>();
    CustomSession customSession;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        handleRequest(req, resp, "POST");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        handleRequest(req, resp, "GET");
    }

    private void handleRequest(HttpServletRequest req, HttpServletResponse resp, String requestMethod) {
        try {
            String urlTaped = req.getServletPath();
            customSession.setHttpSession(req.getSession(true));
            Mapping mapping = road_controller.get(urlTaped);

            if (mapping == null) {
                throw new ServletException("Url not found =>" + urlTaped);
            }

            if (!requestMethod.equals(mapping.getVerb())) {
                throw new ServletException("Access denied for method " + requestMethod+ ", " + mapping.getVerb()  + " required");
            }

            process_request(req, resp, mapping);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public void process_request(HttpServletRequest req, HttpServletResponse res, Mapping mapping) throws Exception {
        PrintWriter print = res.getWriter();
        try {
            // Load the class dynamically
            Class<?> controllerClass = Class.forName(mapping.getClass_name());
            Object controllerInstance = controllerClass.getDeclaredConstructor().newInstance();
            this.handleFields(req, controllerClass, controllerInstance);

            Object returnValue = handleMethod(req, mapping, controllerClass, controllerInstance);

            if (mapping.getMethod().isAnnotationPresent(RestApi.class)) {
                res.setContentType("text/json");
                if (returnValue instanceof ModelAndView modelView) {
                    print.write(json.toJson(modelView.getData()));
                } else {
                    print.write(json.toJson(returnValue));
                }
            } else {
                ////////
                if (returnValue instanceof ModelAndView modelView) {
                    handleModelAndView(modelView, req, res);
                } else if (returnValue instanceof String) {
                    // Print the return value
                    print.println("Return value Method=> " + returnValue);

                } else {
                    throw new IOException("Return type is not supported =>" + returnValue.getClass().getSimpleName());
                }
            }
        } catch (Exception e) {
            for (StackTraceElement ste : e.getStackTrace()) {
                print.println(ste.toString());
            }
            print.println("message = " + e.getMessage());
            if (e.getCause() != null) {
                print.println("cause = " + e.getCause());
            }
        }

    }

    private void handleModelAndView(ModelAndView modelView, HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        Map<String, Object> modelData = modelView.getData();

        for (Map.Entry<String, Object> entry : modelData.entrySet()) {
            req.setAttribute(entry.getKey(), entry.getValue());
        }
        RequestDispatcher dispatcher = req.getRequestDispatcher(modelView.getUrl());
        dispatcher.forward(req, res);
    }

    private void handleFields(HttpServletRequest request, Class controllerClass, Object controllerInstance) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Field[] listFields = controllerClass.getDeclaredFields();
        for (Field field : listFields) {
            if (field.getType() == CustomSession.class) {
                Method setCustomSession = controllerClass.getDeclaredMethod("set" + Tools.capitalize(field.getName()), CustomSession.class);
                setCustomSession.invoke(controllerInstance, this.customSession);
            }
        }
    }

    private Object handleMethod(HttpServletRequest request, Mapping mapping, Class controllerClass, Object controllerInstance) throws Exception {

        // Get the method to be invoked
        Method method = mapping.getMethod();


        Parameter[] parameters = method.getParameters();
        if (parameters.length == 0) {
            return method.invoke(controllerInstance);
        }
        Object[] paramValues = new Object[parameters.length];

        Paranamer paranamer = new BytecodeReadingParanamer();
        String[] paramNames = paranamer.lookupParameterNames(method);

        Requestparam requestparam = new Requestparam(request);
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].getType() == CustomSession.class) {
                // add custom session to paramValues
                paramValues[i] = this.customSession;
            } else if (!parameters[i].isAnnotationPresent(Param.class)) {
                throw new ServletException("ETU 2409 : Annotation Param not found for this method =>" + method.getName());
            } else {
                paramValues[i] = requestparam.mappingParam(parameters[i], paramNames[i]);
            }
        }
        // Invoke the method and get the return value
        return method.invoke(controllerInstance, paramValues);
    }

    public String handleRequestMethod(Method method) {
        if (method.isAnnotationPresent(Get.class)) {
            return "GET";
        } else if (method.isAnnotationPresent(Post.class)) {
            return "POST";
        } else {
            return "GET";
        }
    }


    @Override
    public void init() throws ServletException {
        super.init();
        this.customSession = new CustomSession();
        String directory_controller = getServletContext().getInitParameter("controller");
        String realPath = getServletContext().getRealPath(directory_controller);
        String package_class = directory_controller.split("/")[directory_controller.split("/").length - 1];
        ArrayList<Class<?>> controllers_list = AccesController.getControllerList(package_class, realPath);
        for (Class<?> controller : controllers_list) {
            for (Method method : controller.getMethods()) {
                // existe
                if (method.isAnnotationPresent(Url.class)) {
                    if (road_controller.get(method.getAnnotation(Url.class).road_url()) == null) {
                        road_controller.put(
                                method.getAnnotation(Url.class).road_url(),
                                new Mapping(controller.getName(), method, handleRequestMethod(method))
                        );
                    } else {
                        throw new ServletException("Url already exist =>" + method.getAnnotation(Url.class).road_url());
                    }
                }
            }
        }
    }


}
