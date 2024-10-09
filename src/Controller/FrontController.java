package Controller;

import Annotation.*;
import Model.CustomSession;
import Model.ModelAndView;
import Utils.*;
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
import java.util.*;

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


    private void handleException(HttpServletRequest req, HttpServletResponse res, Exception e) {
        req.setAttribute("errorMessage", e.getMessage());
        res.setStatus(500);
        RequestDispatcher dispatcher = req.getRequestDispatcher("error/error.jsp");
        try {
            dispatcher.forward(req, res);
        } catch (ServletException | IOException ex) {
            ex.printStackTrace();
        }
    }

    private void handleRestApiException(HttpServletResponse res, Exception e) throws IOException {
        res.setContentType("application/json");
        res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        Map<String, Object> errorResponse = new HashMap<>();
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("message", e.getMessage());
        errorDetails.put("status", 500);
        errorDetails.put("details", "An unexpected error occurred");

        errorResponse.put("error", errorDetails);
        PrintWriter out = res.getWriter();
        out.print(json.toJson(errorResponse));
        out.flush();
    }


    private void handleRequest(HttpServletRequest req, HttpServletResponse resp, String requestMethod) {
        try {
            String urlTaped = req.getServletPath();
            customSession.setHttpSession(req.getSession(true));
            Mapping mapping = road_controller.get(urlTaped);

            if (mapping == null) {
                throw new ServletException("Url not found =>" + urlTaped);
            }

            HashSet<VerbAction> verbActions = mapping.getVerbActions();
            Method method = null;
            for (VerbAction verbAction : verbActions) {
                if (requestMethod.equals(verbAction.getVerb())) {
                    method = verbAction.getMethod();
                    break;
                }
            }
            if (method == null) {
                throw new ServletException("Access denied for method " + requestMethod + " not verb found");
            }


            process_request(req, resp,method);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void process_request(HttpServletRequest req, HttpServletResponse res, Method method) throws Exception {
        if (res.isCommitted()) {
            return; // Prevent further response processing if already committed
        }

        PrintWriter print = res.getWriter();
        try {
            Class<?> controllerClass = method.getDeclaringClass();
            Object controllerInstance = controllerClass.getDeclaredConstructor().newInstance();
            this.handleFields(req, controllerClass, controllerInstance);
            Object returnValue = handleMethod(req, method, controllerInstance);

            if (method.isAnnotationPresent(RestApi.class)) {
                res.setContentType("application/json");
                if (!res.isCommitted()) {  // Ensure the response isn't committed
                    if (returnValue instanceof ModelAndView modelView) {
                        print.write(json.toJson(modelView.getData()));
                    } else {
                        print.write(json.toJson(returnValue));
                    }
                }
            } else {
                if (!res.isCommitted()) {  // Ensure response isn't committed for JSP
                    if (returnValue instanceof ModelAndView modelView) {
                        handleModelAndView(modelView, req, res);
                    } else if (returnValue instanceof String) {
                        print.println("Return value Method=> " + returnValue);
                    } else {
                        throw new IOException("Return type is not supported =>" + returnValue.getClass().getSimpleName());
                    }
                }
            }
        } catch (Exception e) {
            if (method.isAnnotationPresent(RestApi.class)) {
                handleRestApiException(res, e);
            } else {
                handleException(req, res, e);
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

    private Object handleMethod(HttpServletRequest request, Method method, Object controllerInstance) throws Exception {

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
                    String url = method.getAnnotation(Url.class).road_url();
                    Mapping mappingCurrent = road_controller.get(url);
                    String verb = handleRequestMethod(method);
                    if (mappingCurrent == null) {
                        VerbAction verbAction = new VerbAction(verb, method);
                        Mapping mp = new Mapping();
                        mp.getVerbActions().add(verbAction);

                        road_controller.put(url, mp);
                    } else {
                        if (!mappingCurrent.getVerbActions().add(new VerbAction(verb, method))) {
                            throw new ServletException("ETU 2409 : Method " + verb + " already exist for this url =>" + url);
                        }
                    }
                }
            }
        }
    }
}
